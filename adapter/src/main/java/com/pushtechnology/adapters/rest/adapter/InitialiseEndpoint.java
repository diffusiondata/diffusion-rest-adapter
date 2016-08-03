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

package com.pushtechnology.adapters.rest.adapter;

import java.util.function.Consumer;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointClient;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;

/**
 * Initialise the endpoint for a service.
 *
 * @author Push Technology Limited
 */
/*package*/ final class InitialiseEndpoint implements Consumer<EndpointConfig> {
    private final EndpointClient endpointClient;
    private final TopicManagementClient topicManagementClient;
    private final ServiceConfig service;
    private final ServiceSession serviceSession;

    /**
     * Constructor.
     */
    /*package*/ InitialiseEndpoint(
            EndpointClient endpointClient,
            TopicManagementClient topicManagementClient,
            ServiceConfig service,
            ServiceSession serviceSession) {

        this.endpointClient = endpointClient;
        this.topicManagementClient = topicManagementClient;
        this.service = service;
        this.serviceSession = serviceSession;
    }

    @Override
    public void accept(EndpointConfig endpointConfig) {
        endpointClient.request(
            service,
            endpointConfig,
            new ValidateContentType(
                endpointConfig,
                new InitialEndpointResponseHandler(
                    topicManagementClient,
                    service,
                    endpointConfig,
                    new AddEndpointToServiceSession(endpointConfig, serviceSession))));
    }
}
