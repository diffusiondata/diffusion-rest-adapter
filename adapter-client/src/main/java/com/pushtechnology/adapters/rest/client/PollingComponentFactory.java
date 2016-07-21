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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.HttpComponent;
import com.pushtechnology.adapters.rest.polling.PollHandlerFactory;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.polling.ServiceSessionImpl;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;

/**
 * Factory for {@link PollingComponent}.
 *s
 * @author Push Technology Limited
 */
public final class PollingComponentFactory {
    private final ScheduledExecutorService executor;

    /**
     * Constructor.
     */
    /*package*/ PollingComponentFactory(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    /**
     * @return a new {@link PollingComponent}
     */
    public PollingComponent create(
            Model model,
            HttpComponent httpComponent,
            PublishingClient publishingClient,
            TopicManagementClient topicManagementClient) {

        final PollHandlerFactory handlerFactory = new PollHandlerFactoryImpl(publishingClient);

        final List<ServiceSession> serviceSessions = new ArrayList<>();
        for (ServiceConfig service : model.getServices()) {
            final ServiceSession serviceSession = new ServiceSessionImpl(
                executor,
                httpComponent,
                service,
                handlerFactory);
            topicManagementClient.addService(service);
            publishingClient
                .addService(service)
                .thenAccept(new ServiceReadyForPublishing(topicManagementClient, serviceSession));
            serviceSessions.add(serviceSession);
        }

        return new PollingComponentImpl(publishingClient, model.getServices(), serviceSessions);
    }
}
