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

import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;

/**
 * Handle the initial response for an endpoint.
 *
 * @author Push Technology Limited
 */
/*package*/ final class InitialEndpointResponseHandler implements FutureCallback<EndpointResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(InitialEndpointResponseHandler.class);
    private final TopicManagementClient topicManagementClient;
    private final ServiceConfig service;
    private final EndpointConfig endpoint;
    private final ServiceSession serviceSession;

    /**
     * Constructor.
     */
    /*package*/ InitialEndpointResponseHandler(
            TopicManagementClient topicManagementClient,
            ServiceConfig service,
            EndpointConfig endpoint,
            ServiceSession serviceSession) {
        this.topicManagementClient = topicManagementClient;
        this.service = service;
        this.endpoint = endpoint;
        this.serviceSession = serviceSession;
    }

    @Override
    public void completed(EndpointResponse result) {
        topicManagementClient.addEndpoint(service, endpoint, new AddEndpointToServiceSession(endpoint, serviceSession));
    }

    @Override
    public void failed(Exception ex) {
        LOG.warn("Initial request to {} failed", endpoint, ex);
    }

    @Override
    public void cancelled() {
        LOG.warn("Initial request to {} cancelled", endpoint);
    }
}
