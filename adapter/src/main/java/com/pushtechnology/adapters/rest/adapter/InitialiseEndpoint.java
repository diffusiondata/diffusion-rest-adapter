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

import java.util.function.Consumer;

import org.apache.http.concurrent.FutureCallback;
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
        final String produces = endpointConfig.getProduces();

        endpointClient.request(
            service,
            endpointConfig,
            new FutureCallback<EndpointResponse>() {
                @Override
                public void completed(EndpointResponse result) {
                    if ("auto".equals(produces)) {
                        new InferTopicType((endpointType) -> {
                            return createTransformingHandler(endpointType, endpointConfig);
                        })
                            .completed(result);
                    }
                    else {
                        final EndpointType<?> endpointType = EndpointType.from(produces);
                        validateContentTypeAndInitialise(result, endpointType, endpointConfig);
                    }
                }

                @Override
                public void failed(Exception ex) {
                    LOG.warn("Endpoint {} not initialised. First request failed.", endpointConfig);
                }

                @Override
                public void cancelled() {
                    LOG.warn("Endpoint {} not initialised. First request cancelled.", endpointConfig);
                }
            });
    }

    private <T> FutureCallback<EndpointResponse> createTransformingHandler(
            EndpointType<T> endpointType,
            EndpointConfig endpointConfig) {
        final EndpointConfig inferredEndpointConfig = EndpointConfig
            .builder()
            .name(endpointConfig.getName())
            .topicPath(endpointConfig.getTopicPath())
            .url(endpointConfig.getUrl())
            .produces(endpointType.getIdentifier())
            .build();
        return new TransformingHandler<>(
            endpointType.getParser(),
            new AddTopicForEndpoint<>(
                topicManagementClient,
                service,
                inferredEndpointConfig,
                publishingClient.createUpdateContext(
                    service,
                    inferredEndpointConfig,
                    endpointType.getDataType()),
                new AddEndpointToServiceSession(inferredEndpointConfig, serviceSession)));
    }

    private <T> void validateContentTypeAndInitialise(
        EndpointResponse result,
        EndpointType<T> endpointType,
        EndpointConfig endpointConfig) {
        new ValidateContentType(
            endpointConfig,
            new TransformingHandler<>(
                endpointType.getParser(),
                new AddTopicForEndpoint<>(
                    topicManagementClient,
                    service,
                    endpointConfig,
                    publishingClient.createUpdateContext(service, endpointConfig, endpointType.getDataType()),
                    new AddEndpointToServiceSession(endpointConfig, serviceSession))))
            .completed(result);
    }
}
