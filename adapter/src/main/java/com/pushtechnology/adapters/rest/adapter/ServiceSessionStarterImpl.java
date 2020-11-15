/*******************************************************************************
 * Copyright (C) 2020 Push Technology Ltd.
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

import static com.pushtechnology.adapters.rest.adapter.AsyncFunction.consume;
import static com.pushtechnology.adapters.rest.endpoints.EndpointType.from;
import static com.pushtechnology.adapters.rest.endpoints.EndpointType.inferFromContentType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.endpoints.EndpointType;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointClient;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.services.ServiceSession;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;

import kotlin.Pair;

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

    /**
     * Constructor.
     */
    public ServiceSessionStarterImpl(
            TopicManagementClient topicManagementClient,
            EndpointClient endpointClient,
            PublishingClient publishingClient,
            ServiceListener serviceListener) {

        this.topicManagementClient = topicManagementClient;
        this.endpointClient = endpointClient;
        this.publishingClient = publishingClient;
        this.serviceListener = serviceListener;
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
                    .forEach(endpointConfig -> initialiseEndpoint(serviceSession, serviceConfig, endpointConfig));
                serviceSession.start();
            })
            .onClose(() -> {
                LOG.info("Service {} closed", serviceConfig);
                serviceListener.onRemove(serviceConfig);
            });
    }

    private void initialiseEndpoint(
            ServiceSession serviceSession,
            ServiceConfig service,
            EndpointConfig endpointConfig) {
        endpointClient
            .request(service, endpointConfig)
            .thenApply(result -> new Pair<>(resolveEndpointConfig(endpointConfig, result), result))
            .thenApply(new ValidateContentType())
            .thenCompose(consume(configAndResult -> handleResponse(
                serviceSession,
                service,
                from(configAndResult.getFirst().getProduces()),
                configAndResult.getFirst(),
                configAndResult.getSecond())))
            .exceptionally(e -> {
                LOG.warn("Endpoint {} not initialised. First request failed. {}", endpointConfig, e.getMessage());
                return null;
            });
    }

    private EndpointConfig resolveEndpointConfig(EndpointConfig endpointConfig, EndpointResponse response) {
        final String produces = endpointConfig.getProduces();
        if ("auto".equals(produces)) {
            return inferEndpointConfig(inferFromContentType(response.getHeader("content-type")), endpointConfig);
        }
        else {
            return endpointConfig;
        }
    }

    private EndpointConfig inferEndpointConfig(EndpointType<?> endpointType, EndpointConfig endpointConfig) {
        return EndpointConfig
            .builder()
            .name(endpointConfig.getName())
            .topicPath(endpointConfig.getTopicPath())
            .url(endpointConfig.getUrl())
            .produces(endpointType.getIdentifier())
            .build();
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private <T> void handleResponse(
        ServiceSession serviceSession,
        ServiceConfig service,
        EndpointType<T> endpointType,
        EndpointConfig endpointConfig,
        EndpointResponse response) throws Exception {

        final T value = endpointType.getParser().transform(response);
        topicManagementClient
            .addEndpoint(service, endpointConfig)
            .thenRun(() -> {
                // If the service has been closed it will have been removed from the publishing client
                publishingClient.forService(service, () -> {
                    LOG.info("Endpoint {} exists, adding endpoint to service session", endpointConfig);
                    serviceSession.addEndpoint(endpointConfig);
                    publishingClient.createUpdateContext(
                        service,
                        endpointConfig,
                        endpointType.getValueType(),
                        endpointType.getDataType()).publish(value);
                });
            })
            .exceptionally(ex -> {
                LOG.warn("Topic creation failed for {} because {}", endpointConfig, ex.getMessage());
                return null;
            });
    }
}
