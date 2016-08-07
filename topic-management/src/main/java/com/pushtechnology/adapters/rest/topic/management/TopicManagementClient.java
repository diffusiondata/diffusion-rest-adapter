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

package com.pushtechnology.adapters.rest.topic.management;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.AddCallback;

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
     * Start managing the topics for a REST endpoint.
     * @param serviceConfig the service of the endpoint
     * @param endpointConfig the endpoint
     * @param callback callback for completion. The onTopicAddFailed with the reason EXISTS.
     */
    void addEndpoint(ServiceConfig serviceConfig, EndpointConfig endpointConfig, AddCallback callback);

    /**
     * Start managing the topics for a REST endpoint.
     * @param serviceConfig the service of the endpoint
     * @param endpointConfig the endpoint
     * @param initialValue the initial value of the topic
     * @param callback callback for completion. The onTopicAddFailed with the reason EXISTS.
     */
    <V> void addEndpoint(
        ServiceConfig serviceConfig,
        EndpointConfig endpointConfig,
        V initialValue,
        AddCallback callback);
}
