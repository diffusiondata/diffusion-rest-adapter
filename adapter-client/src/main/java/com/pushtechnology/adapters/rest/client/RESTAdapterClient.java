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
import java.util.function.Consumer;

import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.HttpClientFactoryImpl;
import com.pushtechnology.adapters.rest.polling.PollClient;
import com.pushtechnology.adapters.rest.polling.PollClientImpl;
import com.pushtechnology.adapters.rest.polling.PollHandlerFactory;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.publication.PublishingClientImpl;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClientImpl;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.AddCallback;
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
    private final PollClient pollClient;
    @GuardedBy("this")
    private PublishingClient publishingClient;
    @GuardedBy("this")
    private TopicManagementClient topicManagementClient;
    @GuardedBy("this")
    private ScheduledExecutorService currentExecutor;
    @GuardedBy("this")
    private Session session;

    private RESTAdapterClient(
            Model model,
            PollClient pollClient) {

        this.model = model;
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

        session = getSession(model.getDiffusion());
        topicManagementClient = new TopicManagementClientImpl(session);
        publishingClient = new PublishingClientImpl(session);
        publishingClient.start();
        pollClient.start();

        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        currentExecutor = executor;

        final PollHandlerFactory handlerFactory = (serviceConfig, endpointConfig) -> new FutureCallback<JSON>() {
            @Override
            public void completed(JSON result) {
                synchronized (RESTAdapterClient.this) {
                    publishingClient.publish(serviceConfig, endpointConfig, result);
                }
            }

            @Override
            public void failed(Exception ex) {
                LOG.warn("Failed to poll endpoint {}", endpointConfig, ex);
            }

            @Override
            public void cancelled() {
                LOG.debug("Polling cancelled for endpoint {}", endpointConfig);
            }
        };

        for (ServiceConfig service : model.getServices()) {
            final ServiceSession serviceSession = new ServiceSession(executor, pollClient, service, handlerFactory);
            topicManagementClient.addService(service);
            publishingClient
                .addService(service)
                .thenAccept(new ServiceReady(serviceSession));
        }
    }

    /**
     * Stop the client.
     * @throws IllegalStateException if the client is not running
     */
    public synchronized void stop() throws IOException {
        final ScheduledExecutorService executor = this.currentExecutor;
        if (executor == null) {
            throw new IllegalStateException("The client is not running");
        }

        publishingClient.stop();
        pollClient.stop();
        session.close();
        executor.shutdown();
        this.currentExecutor = null;
    }

    /**
     * Factory method for {@link RESTAdapterClient}.
     * @param model the configuration to use
     * @return a new {@link RESTAdapterClient}
     */
    public static RESTAdapterClient create(Model model) {
        LOG.debug("Creating REST adapter client with configuration: {}", model);
        final PollClient pollClient = new PollClientImpl(new HttpClientFactoryImpl());

        return new RESTAdapterClient(model, pollClient);
    }

    private Session getSession(DiffusionConfig diffusionConfig) {
        final SessionFactory sessionFactory = Diffusion
            .sessions()
            .serverHost(diffusionConfig.getHost())
            .serverPort(diffusionConfig.getPort())
            .secureTransport(false)
            .transports(WEBSOCKET)
            .reconnectionTimeout(5000)
            .listener(new Listener());

        if (diffusionConfig.getPrincipal() != null && diffusionConfig.getPassword() != null) {
            return sessionFactory
                .principal(diffusionConfig.getPrincipal())
                .password(diffusionConfig.getPassword())
                .open();
        }
        else  {
            return sessionFactory.open();
        }
    }

    /**
     * A simple session state listener that logs out state changes.
     */
    private final class Listener implements Session.Listener {
        @Override
        public void onSessionStateChanged(Session forSession, Session.State oldState, Session.State newState) {
            synchronized (RESTAdapterClient.this) {
                if (currentExecutor == null) {
                    return;
                }

                LOG.warn("{} {} -> {}", forSession, oldState, newState);
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

    private final class ServiceReady implements Consumer<ServiceConfig> {
        private final ServiceSession serviceSession;

        private ServiceReady(ServiceSession serviceSession) {
            this.serviceSession = serviceSession;
        }

        @Override
        public void accept(ServiceConfig serviceConfig) {
            serviceConfig
                .getEndpoints()
                .forEach(endpoint -> {
                    synchronized (RESTAdapterClient.this) {
                        topicManagementClient.addEndpoint(serviceConfig, endpoint, new AddCallback() {
                            @Override
                            public void onTopicAdded(String topicPath) {
                                serviceSession.addEndpoint(endpoint);
                            }

                            @Override
                            public void onTopicAddFailed(String topicPath, TopicAddFailReason reason) {
                                if (reason == TopicAddFailReason.EXISTS) {
                                    onTopicAdded(topicPath);
                                }
                            }

                            @Override
                            public void onDiscard() {
                            }
                        });
                    }
                });
            serviceSession.start();
        }
    }
}
