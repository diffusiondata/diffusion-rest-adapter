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

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.publication.PublishingClient.InitialiseCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;

import net.jcip.annotations.Immutable;

/**
 * Callback for topic creation.
 *
 * @author Push Technology Limited
 */
@Immutable
public final class AddTopicCallback implements TopicControl.AddContextCallback<EndpointConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(AddTopicCallback.class);
    private final ServiceConfig serviceConfig;
    private final InitialiseCallback callback;
    private final AtomicInteger completedCount = new AtomicInteger(0);

    /**
     * Constructor.
     */
    public AddTopicCallback(ServiceConfig serviceConfig, InitialiseCallback callback) {
        this.serviceConfig = serviceConfig;
        this.callback = callback;
    }

    @Override
    public void onTopicAdded(EndpointConfig endpointConfig, String topicPath) {
        LOG.trace("Topic created {}", topicPath);
        callback.onEndpointAdded(serviceConfig, endpointConfig);
        checkComplete();
    }

    @Override
    public void onTopicAddFailed(EndpointConfig endpointConfig, String topicPath, TopicAddFailReason reason) {
        assert (serviceConfig.getTopicRoot() + "/" + endpointConfig.getTopic()).equals(topicPath) :
            "Context used to improve discard logging, expected to be the topic path";

        if (TopicAddFailReason.EXISTS == reason) {
            onTopicAdded(endpointConfig, topicPath);
        }
        else {
            LOG.warn("Failed to add topic {}: {}", topicPath, reason);
            callback.onEndpointFailed(serviceConfig, endpointConfig);
            checkComplete();
        }
    }

    @Override
    public void onDiscard(EndpointConfig endpointConfig) {
        LOG.trace("Failed to add topic {}", serviceConfig.getTopicRoot() + "/" + endpointConfig.getTopic());
        callback.onEndpointFailed(serviceConfig, endpointConfig);
        checkComplete();
    }

    private void checkComplete() {
        if (completedCount.incrementAndGet() == serviceConfig.getEndpoints().size()) {
            callback.onServiceAdded(serviceConfig);
        }
    }
}
