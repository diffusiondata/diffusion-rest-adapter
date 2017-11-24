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

package com.pushtechnology.adapters.rest.services;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;

/**
 * The session for a REST service.
 * <p>
 * Supports multiple endpoints and dynamically changing the endpoints.
 *
 * @author Push Technology Limited
 */
public interface ServiceSession {
    /**
     * Start the session.
     */
    void start();

    /**
     * Add an endpoint to the session.
     */
    void addEndpoint(EndpointConfig endpointConfig);

    /**
     * Start the session.
     */
    void stop();
}
