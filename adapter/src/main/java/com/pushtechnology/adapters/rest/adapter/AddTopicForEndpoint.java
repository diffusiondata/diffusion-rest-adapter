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

import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.publication.UpdateContext;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;

/**
 * Add topic for an endpoint when a response is successfully received.
 *
 * @param <T> the value type of the topic to add
 * @author Push Technology Limited
 */
/*package*/ final class AddTopicForEndpoint<T> implements BiConsumer<T, Throwable> {
    private static final Logger LOG = LoggerFactory.getLogger(AddTopicForEndpoint.class);
    private final TopicManagementClient topicManagementClient;
    private final UpdateContext<T> updateContext;
    private final ServiceConfig service;
    private final EndpointConfig endpoint;
    private final TopicControl.AddCallback callback;

    /**
     * Constructor.
     */
    /*package*/ AddTopicForEndpoint(
            TopicManagementClient topicManagementClient,
            ServiceConfig service,
            EndpointConfig endpoint,
            UpdateContext<T> updateContext,
            TopicControl.AddCallback callback) {

        this.topicManagementClient = topicManagementClient;
        this.updateContext = updateContext;
        this.service = service;
        this.endpoint = endpoint;
        this.callback = callback;
    }

    @Override
    public void accept(T value, Throwable throwable) {
        if (throwable != null) {
            LOG.warn("Initial request to {} failed", endpoint, throwable);
        }
        else {
            topicManagementClient.addEndpoint(service, endpoint, callback);
            updateContext.publish(value);
        }
    }
}
