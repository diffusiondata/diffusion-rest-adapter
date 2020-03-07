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

package com.pushtechnology.adapters.rest.metrics.listeners;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;

/**
 * Listener for endpoint poll events.
 *
 * @author Push Technology Limited
 */
public interface PollListener {
    /**
     * Notified when an attempt to poll an endpoint is made.
     *
     * @param serviceConfig the service
     * @param endpointConfig the endpoint
     * @return a listener for the completion of the poll request
     */
    PollCompletionListener onPollRequest(ServiceConfig serviceConfig, EndpointConfig endpointConfig);

    /**
     * Listener for the completion of a poll request.
     */
    interface PollCompletionListener {
        /**
         * Notified when a response from an endpoint is received.
         *
         * @param response the response from the endpoint
         */
        void onPollResponse(EndpointResponse response);

        /**
         * Notified when an attempt to poll an endpoint fails.
         *
         * @param exception the exception associated with the failure
         */
        void onPollFailure(Exception exception);
    }
}
