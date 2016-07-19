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
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.component.Component;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.PollClient;
import com.pushtechnology.adapters.rest.polling.PollHandlerFactory;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.polling.ServiceSessionImpl;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.publication.PublishingClientImpl;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClientImpl;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;

/**
 * Factory for active components for the {@link RESTAdapterClient}.
 *
 * @author Push Technology Limited
 */
public final class ActiveClientComponentFactory implements RESTAdapterComponentFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveClientComponentFactory.class);

    @Override
    public Component create(Model model, PollClient pollClient, RESTAdapterClientCloseHandle client) {
        final AtomicBoolean isActive = new AtomicBoolean(true);
        final Session session = getSession(model.getDiffusion(), isActive, client);
        final TopicManagementClient topicManagementClient = new TopicManagementClientImpl(session);
        final PublishingClient publishingClient = new PublishingClientImpl(session);

        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        final PollHandlerFactory handlerFactory = new PollHandlerFactoryImpl(publishingClient);

        for (ServiceConfig service : model.getServices()) {
            final ServiceSession serviceSession = new ServiceSessionImpl(executor, pollClient, service, handlerFactory);
            topicManagementClient.addService(service);
            publishingClient
                .addService(service)
                .thenAccept(new ServiceReadyForPublishing(topicManagementClient, serviceSession));
        }

        return () -> {
            isActive.set(false);
            executor.shutdown();
            session.close();
        };
    }

    private static Session getSession(
        DiffusionConfig diffusionConfig,
        AtomicBoolean isActive,
        RESTAdapterClientCloseHandle client) {

        final SessionFactory sessionFactory = Diffusion
            .sessions()
            .serverHost(diffusionConfig.getHost())
            .serverPort(diffusionConfig.getPort())
            .secureTransport(false)
            .transports(WEBSOCKET)
            .reconnectionTimeout(5000)
            .listener(new Listener(isActive, client));

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
     * A {@link Session.Listener} to handle session closes.
     * <p>
     * If the session closes when the state is active the connection and recovery must have failed, stop the client.
     */
    private static final class Listener implements Session.Listener {
        private final AtomicBoolean isActive;
        private final RESTAdapterClientCloseHandle client;

        public Listener(AtomicBoolean isActive, RESTAdapterClientCloseHandle client) {
            this.isActive = isActive;
            this.client = client;
        }

        @Override
        public void onSessionStateChanged(Session forSession, Session.State oldState, Session.State newState) {
            if (isActive.get() && newState.isClosed()) {
                try {
                    client.close();
                }
                catch (IOException e) {
                    LOG.warn("Exception stopping client", e);
                }
            }
        }
    }

}
