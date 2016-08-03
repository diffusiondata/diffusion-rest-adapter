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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;

/**
 * Add the endpoint to the {@link ServiceSession} if the topic is created successfully.
 *
 * @author Push Technology Limited
 */
/*package*/ final class AddEndpointToServiceSession implements TopicControl.AddCallback {
    private static final Logger LOG = LoggerFactory.getLogger(AddEndpointToServiceSession.class);
    private final EndpointConfig endpoint;
    private final ServiceSession serviceSession;

    /**
     * Constructor.
     */
    /*package*/ AddEndpointToServiceSession(EndpointConfig endpoint, ServiceSession serviceSession) {
        this.endpoint = endpoint;
        this.serviceSession = serviceSession;
    }

    @Override
    public void onTopicAdded(String topicPath) {
        LOG.info("Endpoint {} exists, adding endpoint to service session", endpoint);
        serviceSession.addEndpoint(endpoint);
    }

    @Override
    public void onTopicAddFailed(String topicPath, TopicAddFailReason reason) {
        LOG.warn("Topic creation failed for {} because {}", endpoint, reason);
    }

    @Override
    public void onDiscard() {
        LOG.warn("Topic creation for {} might not have succeeded", endpoint);
    }
}
