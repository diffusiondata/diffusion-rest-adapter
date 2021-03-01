/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.services;

import java.util.concurrent.ScheduledExecutorService;

import com.pushtechnology.adapters.rest.metrics.event.listeners.ServiceEventListener;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointClient;
import com.pushtechnology.adapters.rest.polling.EndpointPollHandlerFactory;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;

/**
 * Implementation of {@link ServiceSessionFactory}.
 *
 * @author Push Technology Limited
 */
public final class ServiceSessionFactoryImpl implements ServiceSessionFactory {
    private final ScheduledExecutorService executor;
    private final EndpointClient endpointClient;
    private final EndpointPollHandlerFactory handlerFactory;
    private final TopicManagementClient topicManagementClient;
    private final PublishingClient publishingClient;
    private final ServiceEventListener serviceListener;

    /**
     * Constructor.
     */
    public ServiceSessionFactoryImpl(
            ScheduledExecutorService executor,
            EndpointClient endpointClient,
            EndpointPollHandlerFactory handlerFactory,
            TopicManagementClient topicManagementClient,
            PublishingClient publishingClient,
            ServiceEventListener serviceListener) {
        this.executor = executor;
        this.endpointClient = endpointClient;
        this.handlerFactory = handlerFactory;
        this.topicManagementClient = topicManagementClient;
        this.publishingClient = publishingClient;
        this.serviceListener = serviceListener;
    }

    @Override
    public ServiceSession create(ServiceConfig serviceConfig) {
        return ServiceSessionImpl.create(
            executor,
            endpointClient,
            serviceConfig,
            handlerFactory,
            topicManagementClient,
            publishingClient,
            serviceListener);
    }
}
