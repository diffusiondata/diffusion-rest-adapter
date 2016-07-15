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

package com.pushtechnology.adapters.rest.publication;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Publishing client to update Diffusion.
 *
 * @author Push Technology Limited
 */
public interface PublishingClient {
    /**
     * Start the client running. Connects the client to Diffusion.
     */
    void start();

    /**
     * Add a service to publish to.
     */
    void addService(ServiceConfig serviceConfig, ServiceReadyCallback callback);

    /**
     * Stop the client running.
     */
    void stop();

    /**
     * Update the topic associated with an endpoint.
     */
    void publish(ServiceConfig serviceConfig, EndpointConfig endpointConfig, JSON json);

    /**
     * Callback for when the service is ready.
     */
    interface ServiceReadyCallback {
        /**
         * Notified when the service is ready.
         */
        void onServiceReady(ServiceConfig serviceConfig);
    }
}