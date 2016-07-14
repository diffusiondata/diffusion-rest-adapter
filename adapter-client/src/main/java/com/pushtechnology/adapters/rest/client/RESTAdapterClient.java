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

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.HttpClientFactoryImpl;
import com.pushtechnology.adapters.rest.polling.PollClient;
import com.pushtechnology.adapters.rest.polling.PollClientImpl;
import com.pushtechnology.adapters.rest.polling.PollHandlerFactory;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.publication.PublishingClient.InitialiseCallback;
import com.pushtechnology.adapters.rest.publication.PublishingClientImpl;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;
import com.pushtechnology.diffusion.datatype.json.JSON;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Simple client adapting REST to Diffusion.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class RESTAdapterClient {
    private static final Logger LOG = LoggerFactory.getLogger(RESTAdapterClient.class);

    private final Model model;
    private final PublishingClient publishingClient;
    private final PollClient pollClient;
    @GuardedBy("this")
    private ScheduledExecutorService currentExecutor;

    private RESTAdapterClient(
            Model model,
            PublishingClient publishingClient,
            PollClient pollClient) {

        this.model = model;
        this.publishingClient = publishingClient;
        this.pollClient = pollClient;
    }

    /**
     * Start the client.
     * @throws IllegalStateException if the client is running
     */
    public synchronized void start() {
        if (currentExecutor != null) {
            throw new IllegalStateException("The client is already running");
        }

        publishingClient.start(new Listener());
        pollClient.start();

        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        currentExecutor = executor;

        final PollHandlerFactory handlerFactory = (serviceConfig, endpointConfig) -> new FutureCallback<JSON>() {
            @Override
            public void completed(JSON result) {
                publishingClient.publish(serviceConfig, endpointConfig, result);
            }

            @Override
            public void failed(Exception ex) {
                LOG.warn("Failed to poll endpoint {}", endpointConfig);
            }

            @Override
            public void cancelled() {
                LOG.debug("Polling cancelled for endpoint {}", endpointConfig);
            }
        };

        model
            .getServices()
            .forEach(service -> publishingClient.initialise(
                service,
                new InitialiseCallback() {
                    private final ServiceSession serviceSession =
                        new ServiceSession(executor, pollClient, service, handlerFactory);

                    @Override
                    public void onEndpointAdded(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
                        serviceSession.addEndpoint(endpointConfig);
                    }

                    @Override
                    public void onEndpointFailed(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
                    }

                    @Override
                    public void onServiceAdded(ServiceConfig serviceConfig) {
                        serviceSession.start();
                    }
                }));
    }

    /**
     * Stop the client.
     * @throws IllegalStateException if the client is not running
     */
    public synchronized void stop() throws IOException {
        if (currentExecutor == null) {
            throw new IllegalStateException("The client is not running");
        }

        publishingClient.stop();
        pollClient.stop();
        currentExecutor.shutdown();
        currentExecutor = null;
    }

    /**
     * Factory method for {@link RESTAdapterClient}.
     * @param model the configuration to use
     * @return a new {@link RESTAdapterClient}
     */
    public static RESTAdapterClient create(Model model) {
        LOG.debug("Creating REST adapter client with configuration: {}", model);
        final PublishingClient diffusionClient = new PublishingClientImpl(getSessionFactory(model.getDiffusion()));
        final PollClient pollClient = new PollClientImpl(new HttpClientFactoryImpl());

        return new RESTAdapterClient(model, diffusionClient, pollClient);
    }

    private static SessionFactory getSessionFactory(DiffusionConfig diffusionConfig) {
        final SessionFactory sessionFactory = Diffusion
            .sessions()
            .serverHost(diffusionConfig.getHost())
            .serverPort(diffusionConfig.getPort())
            .secureTransport(false)
            .transports(WEBSOCKET)
            .reconnectionTimeout(5000);

        if (diffusionConfig.getPrincipal() != null && diffusionConfig.getPassword() != null) {
            return sessionFactory
                .principal(diffusionConfig.getPrincipal())
                .password(diffusionConfig.getPassword());
        }
        else  {
            return sessionFactory;
        }
    }

    /**
     * A simple session state listener that logs out state changes.
     */
    private final class Listener implements Session.Listener {
        @Override
        public void onSessionStateChanged(Session session, Session.State oldState, Session.State newState) {
            synchronized (RESTAdapterClient.this) {
                if (currentExecutor == null) {
                    return;
                }

                LOG.warn("{} {} -> {}", session, oldState, newState);
                if (newState.isClosed()) {
                    try {
                        stop();
                    }
                    catch (IOException e) {
                        LOG.warn("Exception stopping client", e);
                    }
                }
            }
        }
    }
}
