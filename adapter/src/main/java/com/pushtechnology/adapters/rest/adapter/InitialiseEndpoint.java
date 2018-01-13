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

package com.pushtechnology.adapters.rest.adapter;

import static com.pushtechnology.adapters.rest.endpoints.EndpointType.from;
import static com.pushtechnology.adapters.rest.endpoints.EndpointType.inferFromContentType;

import java.util.function.Consumer;

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
 * Initialise the endpoint for a service.
 *
 * @author Push Technology Limited
 */
/*package*/ final class InitialiseEndpoint implements Consumer<EndpointConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(InitialiseEndpoint.class);
    private final EndpointClient endpointClient;
    private final TopicManagementClient topicManagementClient;
    private final PublishingClient publishingClient;
    private final ServiceConfig service;
    private final ServiceSession serviceSession;

    /**
     * Constructor.
     */
    /*package*/ InitialiseEndpoint(
            EndpointClient endpointClient,
            TopicManagementClient topicManagementClient,
            PublishingClient publishingClient,
            ServiceConfig service,
            ServiceSession serviceSession) {

        this.endpointClient = endpointClient;
        this.topicManagementClient = topicManagementClient;
        this.publishingClient = publishingClient;
        this.service = service;
        this.serviceSession = serviceSession;
    }

    @Override
    public void accept(EndpointConfig endpointConfig) {
        endpointClient
            .request(service, endpointConfig)
            .thenApply(result -> {
                final EndpointConfig resolvedConfig = resolveEndpointConfig(endpointConfig, result);
                return new Pair<>(resolvedConfig, result);
            })
            .thenApply(configAndResult -> {
                new ValidateContentType().apply(configAndResult.getSecond(), configAndResult.getFirst());
                return configAndResult;
            })
            .thenAccept(configAndResult -> handleResponse(
                from(configAndResult.getFirst().getProduces()),
                configAndResult.getFirst(),
                configAndResult.getSecond()))
            .exceptionally(e -> {
                LOG.warn("Endpoint {} not initialised. First request failed.", endpointConfig);
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

    private <T> void handleResponse(
            EndpointType<T> endpointType,
            EndpointConfig endpointConfig,
            EndpointResponse response) {
        final AddTopicForEndpoint<T> handler = new AddTopicForEndpoint<>(
            topicManagementClient,
            service,
            endpointConfig,
            publishingClient.createUpdateContext(
                service,
                endpointConfig,
                endpointType.getDataType()),
            new AddEndpointToServiceSession(endpointConfig, serviceSession));

        try {
            handler.accept(endpointType.getParser().transform(response), null);
        }
        catch (Exception e) {
            handler.accept(null, e);
        }
    }
}
