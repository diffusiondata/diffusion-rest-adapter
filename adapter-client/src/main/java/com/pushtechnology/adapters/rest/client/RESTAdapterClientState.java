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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
import com.pushtechnology.diffusion.client.session.Session;

/**
 * The current state of the {@link RESTAdapterClient}.
 *
 * @author Push Technology Limited
 */
/*package*/ final class RESTAdapterClientState implements AutoCloseable {
    private final PublishingClient publishingClient;
    private final ScheduledExecutorService currentExecutor;
    private final Session session;

    /*package*/ RESTAdapterClientState(
            PublishingClient publishingClient,
            ScheduledExecutorService currentExecutor,
            Session session) {

        this.publishingClient = publishingClient;
        this.currentExecutor = currentExecutor;
        this.session = session;
    }

    @Override
    public void close() throws IOException {
        publishingClient.stop();
        currentExecutor.shutdown();
        session.close();
    }

    /*package*/ static RESTAdapterClientState create(Model model, PollClient pollClient, Session session) {
        final TopicManagementClient topicManagementClient = new TopicManagementClientImpl(session);
        final PublishingClient publishingClient = new PublishingClientImpl(session);
        publishingClient.start();

        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        final PollHandlerFactory handlerFactory = (serviceConfig, endpointConfig) -> new PollPublishingHandler(
            publishingClient, serviceConfig, endpointConfig);

        for (ServiceConfig service : model.getServices()) {
            final ServiceSession serviceSession = new ServiceSessionImpl(executor, pollClient, service, handlerFactory);
            topicManagementClient.addService(service);
            publishingClient
                .addService(service)
                .thenAccept(new ServiceReadyForPublishing(topicManagementClient, serviceSession));
        }

        return new RESTAdapterClientState(publishingClient, executor, session);
    }
}
