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

package com.pushtechnology.adapters.rest.metrics.listeners;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;

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
    TopicCreationCompletionListener onTopicCreationRequest(
        ServiceConfig serviceConfig,
        EndpointConfig endpointConfig);

    /**
     * Listener for the completion of a topic creation request.
     */
    interface TopicCreationCompletionListener {
        /**
         * Notified when a Diffusion topic is created.
         */
        void onTopicCreated();

        /**
         * Notified when a Diffusion topic cannot be created.
         *
         * @param reason the cause of failure
         */
        void onTopicCreationFailed(TopicAddFailReason reason);
    }
}
