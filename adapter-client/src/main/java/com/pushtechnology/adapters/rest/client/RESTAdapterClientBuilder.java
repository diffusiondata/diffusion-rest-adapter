/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

import static com.pushtechnology.adapters.rest.adapter.ServiceListener.NULL_LISTENER;
import static com.pushtechnology.adapters.rest.model.conversion.ConversionContext.FULL_CONTEXT;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;

import com.pushtechnology.adapters.rest.adapter.ServiceListener;
import com.pushtechnology.adapters.rest.model.store.ModelStore;
import com.pushtechnology.adapters.rest.model.store.PollingPersistedModelStore;
import com.pushtechnology.adapters.rest.persistence.FileSystemPersistence;
import com.pushtechnology.adapters.rest.persistence.Persistence;
import com.pushtechnology.diffusion.client.session.Session;

import net.jcip.annotations.Immutable;

/**
 * Builder for the {@link RESTAdapterClient}.
 *
 * @author Matt Champion 25/06/2017
 */
@Immutable
public final class RESTAdapterClientBuilder {
    private final ModelStore modelStore;
    private final ScheduledExecutorService executor;
    private final Runnable shutdownHandler;
    private final ServiceListener serviceListener;
    private final Path pathToConfigDirectory;
    private final Session.Listener sessionListener;

    /*package*/ RESTAdapterClientBuilder() {
        this.executor = null;
        this.serviceListener = null;
        this.modelStore = null;
        this.shutdownHandler = null;
        this.pathToConfigDirectory = null;
        this.sessionListener = null;
    }

    /*package*/ RESTAdapterClientBuilder(
            ModelStore modelStore,
            ScheduledExecutorService executor,
            Runnable shutdownHandler,
            ServiceListener serviceListener,
            Path pathToConfigDirectory,
            Session.Listener sessionListener) {
        this.executor = executor;
        this.serviceListener = serviceListener;
        this.modelStore = modelStore;
        this.shutdownHandler = shutdownHandler;
        this.pathToConfigDirectory = pathToConfigDirectory;
        this.sessionListener = sessionListener;
    }

    /**
     * Use the provided executor.
     */
    public RESTAdapterClientBuilder executor(ScheduledExecutorService newExecutor) {
        return new RESTAdapterClientBuilder(
            modelStore,
            newExecutor,
            shutdownHandler,
            serviceListener,
            pathToConfigDirectory,
            sessionListener);
    }

    /**
     * Use the provided service listener.
     */
    public RESTAdapterClientBuilder serviceListener(ServiceListener newServiceListener) {
        return new RESTAdapterClientBuilder(
            modelStore,
            executor,
            shutdownHandler,
            newServiceListener,
            pathToConfigDirectory,
            sessionListener);
    }

    /**
     * Use the provided shutdown handler.
     */
    public RESTAdapterClientBuilder shutdownHandler(Runnable newShutdownHandler) {
        return new RESTAdapterClientBuilder(
            modelStore,
            executor,
            newShutdownHandler,
            serviceListener,
            pathToConfigDirectory,
            sessionListener);
    }

    /**
     * Use the provided path to configuration directory.
     */
    public RESTAdapterClientBuilder pathToConfigDirectory(Path newPathToConfigDirectory) {
        return new RESTAdapterClientBuilder(
            modelStore,
            executor,
            shutdownHandler,
            serviceListener,
            newPathToConfigDirectory,
            sessionListener);
    }

    /**
     * Use the provided model store.
     */
    public RESTAdapterClientBuilder modelStore(ModelStore newModelStore) {
        return new RESTAdapterClientBuilder(
            newModelStore,
            executor,
            shutdownHandler,
            serviceListener,
            pathToConfigDirectory,
            sessionListener);
    }

    /**
     * Use the provided session listener.
     */
    public RESTAdapterClientBuilder sessionListener(Session.Listener newSessionListener) {
        return new RESTAdapterClientBuilder(
            modelStore,
            executor,
            shutdownHandler,
            serviceListener,
            pathToConfigDirectory,
            newSessionListener);
    }

    private RESTAdapterClient create() {
        return new RESTAdapterClient(modelStore, executor, shutdownHandler, serviceListener, sessionListener);
    }
    private RESTAdapterClientBuilder finaliseBuilder() throws IOException {
        if (shutdownHandler == null) {
            return shutdownHandler(() -> { }).finaliseBuilder();
        }

        if (executor == null) {
            final ScheduledExecutorService newExecutor = newSingleThreadScheduledExecutor();
            return new RESTAdapterClientBuilder(
                    modelStore,
                    newExecutor,
                    () -> {
                        newExecutor.shutdown();
                        shutdownHandler.run();
                    },
                    serviceListener,
                    pathToConfigDirectory,
                    sessionListener)
                .finaliseBuilder();
        }

        if (serviceListener == null) {
            return serviceListener(NULL_LISTENER).finaliseBuilder();
        }

        if (modelStore == null && pathToConfigDirectory == null) {
            throw new IllegalStateException("Unable to find model store or configuration directory");
        }

        if (modelStore == null) {
            final Persistence fileSystemPersistence = new FileSystemPersistence(pathToConfigDirectory, FULL_CONTEXT);
            final PollingPersistedModelStore newModelStore = new PollingPersistedModelStore(
                fileSystemPersistence,
                executor,
                1000L);

            try {
                newModelStore.start();
            }
            catch (IllegalStateException | IOException e) {
                executor.shutdown();
                throw e;
            }

            return new RESTAdapterClientBuilder(
                newModelStore,
                executor,
                () -> {
                    newModelStore.stop();
                    shutdownHandler.run();
                },
                serviceListener,
                pathToConfigDirectory,
                sessionListener)
                .finaliseBuilder();
        }

        if (sessionListener == null) {
            return new RESTAdapterClientBuilder(
                modelStore,
                executor,
                shutdownHandler,
                serviceListener,
                pathToConfigDirectory,
                (session, oldState, newState) -> { })
                .finaliseBuilder();
        }

        return this;
    }


    /**
     * @return a new REST adapter client
     */
    public RESTAdapterClient build() throws IOException {
        return finaliseBuilder().create();
    }
}
