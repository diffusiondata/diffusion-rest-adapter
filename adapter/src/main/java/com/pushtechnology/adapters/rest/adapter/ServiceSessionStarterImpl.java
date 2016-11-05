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

package com.pushtechnology.adapters.rest.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointClient;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;

/**
 * Implementation for {@link ServiceSessionStarter}.
 * @author Push Technology Limited
 */
public final class ServiceSessionStarterImpl implements ServiceSessionStarter {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceSessionStarterImpl.class);

    private final TopicManagementClient topicManagementClient;
    private final EndpointClient endpointClient;
    private final PublishingClient publishingClient;
    private final ServiceListener serviceListener;
    private final ParserFactory parserFactory;

    /**
     * Constructor.
     */
    public ServiceSessionStarterImpl(
            TopicManagementClient topicManagementClient,
            EndpointClient endpointClient,
            PublishingClient publishingClient,
            ServiceListener serviceListener,
            ParserFactory parserFactory) {
        this.topicManagementClient = topicManagementClient;
        this.endpointClient = endpointClient;
        this.publishingClient = publishingClient;
        this.serviceListener = serviceListener;
        this.parserFactory = parserFactory;
    }

    @Override
    public void start(ServiceConfig serviceConfig, ServiceSession serviceSession) {
        topicManagementClient.addService(serviceConfig);
        publishingClient
            .addService(serviceConfig)
            .onStandby(() -> {
                LOG.info("Service {} on standby", serviceConfig);
                serviceListener.onStandby(serviceConfig);
            })
            .onActive((updater) -> {
                LOG.info("Service {} active", serviceConfig);
                serviceListener.onActive(serviceConfig);
                serviceConfig
                    .getEndpoints()
                    .forEach(new InitialiseEndpoint(
                        endpointClient,
                        topicManagementClient,
                        serviceConfig,
                        serviceSession,
                        parserFactory));
                serviceSession.start();
            })
            .onClose(() -> {
                LOG.info("Service {} closed", serviceConfig);
                serviceListener.onRemove(serviceConfig);
            });
    }
}
