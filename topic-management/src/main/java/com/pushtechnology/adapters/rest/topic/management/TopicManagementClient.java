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

package com.pushtechnology.adapters.rest.topic.management;

import java.util.concurrent.CompletableFuture;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.AddCallback;
import com.pushtechnology.diffusion.client.topics.details.TopicType;

/**
 * Topic management client for Diffusion server.
 *
 * @author Push Technology Limited
 */
public interface TopicManagementClient {
    /**
     * Start managing the topics for a REST service.
     */
    void addService(ServiceConfig serviceConfig);

    /**
     * Start managing the topic for a REST endpoint.
     * @param serviceConfig the service of the endpoint
     * @param endpointConfig the endpoint
     * @param callback callback for completion. The onTopicAddFailed with the reason EXISTS.
     */
    void addEndpoint(ServiceConfig serviceConfig, EndpointConfig endpointConfig, AddCallback callback);

    /**
     * Remove the topic for a REST endpoint.
     * @param serviceConfig the service of the endpoint
     * @param endpointConfig the endpoint
     */
    void removeEndpoint(ServiceConfig serviceConfig, EndpointConfig endpointConfig);

    /**
     * Stop managing the topics for a REST service.
     */
    void removeService(ServiceConfig serviceConfig);

    /**
     * Add a topic.
     * @param path the path
     * @param topicType the topic type
     * @return a future representing the completion of adding the topic
     */
    CompletableFuture<Void> addTopic(String path, TopicType topicType);
}
