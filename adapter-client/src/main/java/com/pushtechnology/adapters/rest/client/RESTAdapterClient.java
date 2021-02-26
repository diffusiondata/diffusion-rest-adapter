/*******************************************************************************
 * Copyright (C) 2020 Push Technology Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.pushtechnology.adapters.rest.client;

import static com.pushtechnology.adapters.rest.model.conversion.ConversionContext.FULL_CONTEXT;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.adapter.InternalRESTAdapter;
import com.pushtechnology.adapters.rest.adapter.ServiceListener;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.store.ModelStore;
import com.pushtechnology.adapters.rest.model.store.PollingPersistedModelStore;
import com.pushtechnology.adapters.rest.persistence.FileSystemPersistence;
import com.pushtechnology.adapters.rest.persistence.Persistence;
import com.pushtechnology.adapters.rest.polling.HttpClientFactoryImpl;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.session.Session;

import net.jcip.annotations.ThreadSafe;

/**
 * Simple client adapting REST to Diffusion.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class RESTAdapterClient {
    private static final Logger LOG = LoggerFactory.getLogger(RESTAdapterClient.class);

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final InternalRESTAdapter restAdapter;
    private final ModelStore modelStore;
    private final Runnable shutdownHandler;

    /*package*/ RESTAdapterClient(
            Path relativePath,
            ModelStore modelStore,
            ScheduledExecutorService executor,
            Runnable shutdownHandler,
            ServiceListener serviceListener,
            Session.Listener listener) {
        this.modelStore = modelStore;
        this.shutdownHandler = shutdownHandler;
        restAdapter = new InternalRESTAdapter(
            relativePath,
            executor,
            Diffusion.sessions(),
            new HttpClientFactoryImpl(),
            serviceListener,
            () -> {
                isRunning.set(false);
                shutdownHandler.run();
            },
            () -> {
                isRunning.set(false);
                shutdownHandler.run();
            },
            listener);
    }

    /**
     * Start the client.
     * @throws IllegalStateException if the client is running
     */
    public synchronized void start() {
        if (!isRunning.compareAndSet(false, true)) {
            throw new IllegalStateException("The client is already running");
        }

        modelStore.onModelChange(this::onModelChange);
    }

    private void onModelChange(Model newModel) {
        LOG.debug("Running REST adapter client with model : {}", newModel);

        try {
            restAdapter.onReconfiguration(newModel);
        }
        catch (IllegalArgumentException e) {
            LOG.warn("The new model is not valid", e);
        }
        // CHECKSTYLE.OFF: IllegalCatch // Bulkhead
        catch (Exception e) {
            LOG.warn("There was a problem applying the new model", e);
        }
        // CHECKSTYLE.ON: IllegalCatch
    }

    /**
     * Stop the client.
     * @throws IllegalStateException if the client is not running
     */
    public synchronized void close() throws IOException {
        if (!isRunning.compareAndSet(true, false)) {
            throw new IllegalStateException("The client is not running");
        }

        restAdapter.close();

        shutdownHandler.run();
    }

    /**
     * Factory method for {@link RESTAdapterClient}.
     * @param modelStore the configuration store to use
     * @param executor executor to use to schedule poll requests
     * @return a new {@link RESTAdapterClient}
     */
    public static RESTAdapterClient create(
        Path relativePath,
        ModelStore modelStore,
        ScheduledExecutorService executor,
        Runnable shutdownHandler,
        ServiceListener serviceListener,
        Session.Listener listener) {
        LOG.debug("Creating REST adapter client with model store: {}", modelStore);
        return new RESTAdapterClient(relativePath, modelStore, executor, shutdownHandler, serviceListener, listener);
    }

    /**
     * Factory method for {@link RESTAdapterClient}.
     * @param pathToConfigDirectory the directory with the configuration
     * @return a new {@link RESTAdapterClient}
     * @throws IOException if there is a problem with accessing the model store
     * @throws IllegalStateException if the configuration is missing
     */
    @SuppressWarnings("PMD.DoNotCallSystemExit")
    public static RESTAdapterClient create(Path pathToConfigDirectory) throws IOException {
        final Persistence fileSystemPersistence = new FileSystemPersistence(pathToConfigDirectory, FULL_CONTEXT);
        final ScheduledExecutorService executor = newSingleThreadScheduledExecutor();
        final PollingPersistedModelStore modelStore = new PollingPersistedModelStore(
            fileSystemPersistence,
            executor,
            1000L);

        try {
            modelStore.start();
        }
        catch (IllegalStateException | IOException e) {
            executor.shutdown();
            throw e;
        }

        return RESTAdapterClient.create(
            pathToConfigDirectory,
            modelStore,
            executor,
            () -> {
                modelStore.stop();
                executor.shutdown();
                if (modelStore.get().getMetrics().getPrometheus() != null) {
                    // Needed because not all prometheus exporter threads are daemons
                    Runtime.getRuntime().exit(0);
                }
            },
            ServiceListener.NULL_LISTENER,
            (session, oldState, newState) ->
                LOG.debug("Session state change {} {} -> {}", session, oldState, newState));
    }
}
