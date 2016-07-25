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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.HttpComponent;
import com.pushtechnology.adapters.rest.polling.PollHandlerFactory;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.polling.ServiceSessionImpl;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;

/**
 * The component responsible for polling REST services.
 *
 * @author Push Technology Limited
 */
public final class PollingComponentImpl implements PollingComponent {
    private static final Logger LOG = LoggerFactory.getLogger(PollingComponentImpl.class);
    private final Model model;
    private final ScheduledExecutorService executor;
    private final HttpComponent httpComponent;
    private final TopicManagementClient topicManagementClient;
    private final PublishingClient publishingClient;
    private final List<ServiceSession> serviceSessions;

    /**
     * Constructor.
     */
    public PollingComponentImpl(
            Model model,
            ScheduledExecutorService executor,
            HttpComponent httpComponent,
            TopicManagementClient topicManagementClient,
            PublishingClient publishingClient) {
        this.model = model;
        this.executor = executor;
        this.httpComponent = httpComponent;
        this.topicManagementClient = topicManagementClient;
        this.publishingClient = publishingClient;
        this.serviceSessions = new ArrayList<>();
    }

    @PostConstruct
    @Override
    public synchronized void start() {
        LOG.info("Opening polling component");
        final PollHandlerFactory handlerFactory = new PollHandlerFactoryImpl(publishingClient);
        for (final ServiceConfig service : model.getServices()) {
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
        LOG.info("Opened polling component");
    }

    @PreDestroy
    @Override
    public synchronized void close() {
        LOG.info("Closing polling component");
        serviceSessions.forEach(ServiceSession::stop);
        model.getServices().forEach(publishingClient::removeService);
        LOG.info("Closed polling component");
    }
}
