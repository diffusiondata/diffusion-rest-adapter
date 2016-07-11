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

import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;

/**
 * Callback for topic creation.
 *
 * @author Push Technology Limited
 */
public enum AddTopicCallback implements TopicControl.AddContextCallback<String> {
    /**
     * Callback instance.
     */
    INSTANCE;

    private static final Logger LOG = LoggerFactory.getLogger(AddTopicCallback.class);

    @Override
    public void onTopicAdded(String context, String topicPath) {
        assert context.equals(topicPath) : "Context used to improve discard logging, expected to be the topic path";

        LOG.trace("Topic created {}", topicPath);
    }

    @Override
    public void onTopicAddFailed(String context, String topicPath, TopicAddFailReason reason) {
        assert context.equals(topicPath) : "Context used to improve discard logging, expected to be the topic path";

        if (TopicAddFailReason.EXISTS == reason) {
            onTopicAdded(context, topicPath);
        }
        else {
            LOG.warn("Failed to add topic {}: {}", topicPath, reason);
        }
    }

    @Override
    public void onDiscard(String context) {
        LOG.trace("Failed to add topic {}", context);
    }
}
