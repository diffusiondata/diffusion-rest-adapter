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

package com.pushtechnology.adapters.rest.integrated.server;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ScheduledExecutorService;

import javax.naming.NamingException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.adapter.ServiceListener;
import com.pushtechnology.adapters.rest.client.RESTAdapterClient;
import com.pushtechnology.adapters.rest.client.controlled.model.store.ClientControlledModelStore;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig.DiffusionConfigBuilder;
import com.pushtechnology.diffusion.client.session.Session;

/**
 * Diffusion REST Adapter Integrated Server.
 *
 * <p>
 * Combines the Diffusion REST Adapter client and the web interface hosted on a Jetty server.
 *
 * @author Push Technology Limited
 */
public final class RESTAdapterIntegratedServer implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(RESTAdapterIntegratedServer.class);
    private final Server jettyServer;
    private final RESTAdapterClient adapterClient;
    private final ClientControlledModelStore modelStore;

    private RESTAdapterIntegratedServer(
            Server jettyServer,
            RESTAdapterClient adapterClient,
            ClientControlledModelStore modelStore) {
        this.jettyServer = jettyServer;
        this.adapterClient = adapterClient;
        this.modelStore = modelStore;
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
    public static RESTAdapterIntegratedServer create(
            int port,
            DiffusionConfigBuilder baseConfig) throws NamingException, IOException {

        return create(port, baseConfig, null);
    }

    /**
     * @param port the port for the application server to listen on
     * @return a new {@link RESTAdapterIntegratedServer}
     */
    public static RESTAdapterIntegratedServer create(
        int port,
        DiffusionConfigBuilder baseConfig,
        String truststore) throws NamingException, IOException {

        final Path tempFile = Files.createTempFile("web-interface-servlet", ".war");
        extractWarTo(tempFile);

        final ScheduledExecutorService executor = newSingleThreadScheduledExecutor();

        final DiffusionConfig diffusionConfig = baseConfig.build();
        final ClientControlledModelStore modelStore = new ClientControlledModelStore(
            executor,
            diffusionConfig,
            truststore);

        final Server jettyServer = new Server(port);
        final WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setInitParameter("host", diffusionConfig.getHost());
        webapp.setInitParameter("port", "" + diffusionConfig.getPort());
        webapp.setInitParameter("secure", "" + diffusionConfig.isSecure());
        webapp.setWar(tempFile.toAbsolutePath().toString());
        final GzipHandler gzipHandler = new GzipHandler();
        gzipHandler.setHandler(webapp);
        jettyServer.setHandler(gzipHandler);

        final RESTAdapterClient adapterClient = RESTAdapterClient.create(
            modelStore,
            executor,
            executor::shutdown,
            ServiceListener.NULL_LISTENER,
            (session, oldState, newState) -> {
                if (oldState == Session.State.CONNECTING && newState.isConnected()) {
                    modelStore.onSessionReady(session);
                }
            });

        LOG.info("Running web interface on {}", port);

        return new RESTAdapterIntegratedServer(jettyServer, adapterClient, modelStore);
    }

    private static void extractWarTo(Path warFile) throws IOException {
        final URL servlet = Thread.currentThread().getContextClassLoader().getResource("web-interface-servlet.war");
        if (servlet == null) {
            throw new IllegalStateException("Web interface not located");
        }
        final InputStream inputStream = servlet.openStream();
        final OutputStream outputStream = Files.newOutputStream(warFile, StandardOpenOption.TRUNCATE_EXISTING);

        // Copy resource to temporary file
        do {
            final int nextByte = inputStream.read();
            if (nextByte != -1) {
                outputStream.write(nextByte);
            }
            else {
                break;
            }
        } while (true);

        inputStream.close();
        outputStream.close();
    }
}
