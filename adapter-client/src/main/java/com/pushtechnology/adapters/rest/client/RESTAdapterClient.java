/*******************************************************************************
 * Copyright (C) 2016 Push Technology Ltd.
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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.store.ModelStore;
import com.pushtechnology.adapters.rest.polling.HttpClientFactoryImpl;
import com.pushtechnology.adapters.rest.polling.PollClient;
import com.pushtechnology.adapters.rest.polling.PollClientImpl;

import net.jcip.annotations.ThreadSafe;

/**
 * Simple client adapting REST to Diffusion.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class RESTAdapterClient implements RESTAdapterClientCloseHandle {
    private static final Logger LOG = LoggerFactory.getLogger(RESTAdapterClient.class);

    private final ClientComponent clientComponent = new ClientComponent();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ModelStore modelStore;
    private final PollClient pollClient;

    private RESTAdapterClient(ModelStore modelStore, PollClient pollClient) {
        this.modelStore = modelStore;
        this.pollClient = pollClient;
    }

    /**
     * Start the client.
     * @throws IllegalStateException if the client is running
     */
    public synchronized void start() {
        if (!isRunning.compareAndSet(false, true)) {
            throw new IllegalStateException("The client is already running");
        }

        pollClient.start();
        modelStore.onModelChange(this::onModelChange);
    }

    private void onModelChange(Model newModel) {
        LOG.debug("Running REST adapter client with model : {}", newModel);

        try {
            clientComponent.reconfigure(newModel, pollClient, this);
        }
        catch (IOException e) {
            LOG.warn("Failed to shutdown previous model on model change");
        }
    }

    /**
     * Stop the client.
     * @throws IllegalStateException if the client is not running
     */
    public synchronized void close() throws IOException {
        if (!isRunning.compareAndSet(true, false)) {
            throw new IllegalStateException("The client is not running");
        }

        clientComponent.close();
        pollClient.stop();
    }

    /**
     * Factory method for {@link RESTAdapterClient}.
     * @param modelStore the configuration store to use
     * @return a new {@link RESTAdapterClient}
     */
    public static RESTAdapterClient create(ModelStore modelStore) {
        LOG.debug("Creating REST adapter client with model store: {}", modelStore);
        final PollClient pollClient = new PollClientImpl(new HttpClientFactoryImpl());

        return new RESTAdapterClient(modelStore, pollClient);
    }
}
