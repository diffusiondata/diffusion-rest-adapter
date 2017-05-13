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

package com.pushtechnology.adapters.rest.metrics;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Listener for Diffusion topic creation events.
 *
 * @author Push Technology Limited
 */
public interface TopicCreationListener {
    /**
     * Notified when an attempt to create a Diffusion topic is made and there is no initial value.
     *
     * @param serviceConfig the service
     * @param endpointConfig the endpoint
     */
    void onTopicCreationRequest(ServiceConfig serviceConfig, EndpointConfig endpointConfig);

    /**
     * Notified when an attempt to create a Diffusion topic is made and there is an initial value.
     *
     * @param serviceConfig the service
     * @param endpointConfig the endpoint
     * @param value the initial value
     */
    void onTopicCreationRequest(ServiceConfig serviceConfig, EndpointConfig endpointConfig, Bytes value);

    /**
     * Notified when a Diffusion topic is created and there is no initial value.
     *
     * @param serviceConfig the service
     * @param endpointConfig the endpoint
     */
    void onTopicCreated(ServiceConfig serviceConfig, EndpointConfig endpointConfig);

    /**
     * Notified when a Diffusion topic is created and there is an initial value.
     *
     * @param serviceConfig the service
     * @param endpointConfig the endpoint
     * @param value the initial value
     */
    void onTopicCreated(ServiceConfig serviceConfig, EndpointConfig endpointConfig, Bytes value);

    /**
     * Notified when a Diffusion topic cannot be created and there is no initial value.
     *
     * @param serviceConfig the service
     * @param endpointConfig the endpoint
     * @param reason the cause of failure
     */
    void onTopicCreationFailed(
        ServiceConfig serviceConfig,
        EndpointConfig endpointConfig,
        TopicAddFailReason reason);

    /**
     * Notified when a Diffusion topic cannot be created and there is an initial value.
     *
     * @param serviceConfig the service
     * @param endpointConfig the endpoint
     * @param value the initial value
     * @param reason the cause of failure
     */
    void onTopicCreationFailed(
        ServiceConfig serviceConfig,
        EndpointConfig endpointConfig,
        Bytes value,
        TopicAddFailReason reason);

    /**
     * Implementation of {@link TopicCreationListener} that ignores notifications.
     */
    TopicCreationListener NULL_LISTENER = new TopicCreationListener() {
        @Override
        public void onTopicCreationRequest(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        }

        @Override
        public void onTopicCreationRequest(ServiceConfig serviceConfig, EndpointConfig endpointConfig, Bytes value) {
        }

        @Override
        public void onTopicCreated(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        }

        @Override
        public void onTopicCreated(ServiceConfig serviceConfig, EndpointConfig endpointConfig, Bytes value) {
        }

        @Override
        public void onTopicCreationFailed(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            TopicAddFailReason reason) {
        }

        @Override
        public void onTopicCreationFailed(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            Bytes value,
            TopicAddFailReason reason) {
        }
    };
}
