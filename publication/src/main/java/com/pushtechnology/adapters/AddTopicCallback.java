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

package com.pushtechnology.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.PublishingClient.InitialiseCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;

import net.jcip.annotations.Immutable;

/**
 * Callback for topic creation.
 *
 * @author Push Technology Limited
 */
@Immutable
public final class AddTopicCallback implements TopicControl.AddCallback {
    private static final Logger LOG = LoggerFactory.getLogger(AddTopicCallback.class);
    private final String expectedTopicPath;
    private final InitialiseCallback callback;

    /**
     * Constructor.
     */
    public AddTopicCallback(String expectedTopicPath, InitialiseCallback callback) {
        this.expectedTopicPath = expectedTopicPath;

        this.callback = callback;
    }

    @Override
    public void onTopicAdded(String topicPath) {
        LOG.trace("Topic created {}", topicPath);
        callback.onTopicAdded(topicPath);
    }

    @Override
    public void onTopicAddFailed(String topicPath, TopicAddFailReason reason) {
        assert expectedTopicPath.equals(topicPath) :
            "Context used to improve discard logging, expected to be the topic path";

        if (TopicAddFailReason.EXISTS == reason) {
            onTopicAdded(topicPath);
        }
        else {
            LOG.warn("Failed to add topic {}: {}", topicPath, reason);
        }
    }

    @Override
    public void onDiscard() {
        LOG.trace("Failed to add topic {}", expectedTopicPath);
    }
}
