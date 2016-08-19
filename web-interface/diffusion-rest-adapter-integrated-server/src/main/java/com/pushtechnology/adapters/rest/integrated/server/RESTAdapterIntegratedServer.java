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

package com.pushtechnology.adapters.rest.integrated.server;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.util.concurrent.ScheduledExecutorService;

import javax.naming.NamingException;

import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.server.Server;

import com.pushtechnology.adapters.rest.adapter.ServiceListener;
import com.pushtechnology.adapters.rest.client.RESTAdapterClient;
import com.pushtechnology.adapters.rest.model.store.AsyncMutableModelStore;

/**
 * Diffusion REST Adapter Integrated Server.
 *
 * <p>
 * Combines the Diffusion REST Adapter client and the web interface hosted on a Jetty server.
 *
 * @author Push Technology Limited
 */
public final class RESTAdapterIntegratedServer implements AutoCloseable {
    private final Server jettyServer;
    private final RESTAdapterClient adapterClient;

    private RESTAdapterIntegratedServer(Server jettyServer, RESTAdapterClient adapterClient) {
        this.jettyServer = jettyServer;
        this.adapterClient = adapterClient;
    }

    /**
     * Start the server.
     */
    public void start() {
        try {
            jettyServer.start();
            adapterClient.start();
        }
        // CHECKSTYLE.OFF: IllegalCatch // Jetty throws Exceptions
        catch (Exception e) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        try {
            adapterClient.close();
            jettyServer.stop();
        }
        // CHECKSTYLE.OFF: IllegalCatch // Jetty throws Exceptions
        catch (Exception e) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param port the port for the application server to listen on
     * @return a new {@link RESTAdapterIntegratedServer}
     */
    public static RESTAdapterIntegratedServer create(int port) throws NamingException {
        final ScheduledExecutorService executor = newSingleThreadScheduledExecutor();

        final AsyncMutableModelStore modelStore = new AsyncMutableModelStore(executor);

        final Server jettyServer = new Server(port);
        // The constructor appears to bind itself using static methods
        new Resource(jettyServer, "diffusion/rest/modelstore", modelStore);

        final RESTAdapterClient adapterClient = RESTAdapterClient.create(
            modelStore,
            executor,
            executor::shutdown,
            ServiceListener.NULL_LISTENER);

        return new RESTAdapterIntegratedServer(jettyServer, adapterClient);
    }
}
