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

package com.pushtechnology.adapters.rest.topic.management;

import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;

/**
 * Callback handles mapping of {@link TopicAddFailReason#EXISTS} to success.
 *
 * @author Push Technology Limited
 */
public final class TopicSetupCallback implements TopicControl.AddCallback {
    private final ListenerNotifier listenerNotifier;
    private final TopicControl.AddCallback delegate;

    /**
     * Constructor.
     */
    public TopicSetupCallback(ListenerNotifier listenerNotifier, TopicControl.AddCallback delegate) {
        this.listenerNotifier = listenerNotifier;
        this.delegate = delegate;
    }

    @Override
    public void onTopicAdded(String topicPath) {
        listenerNotifier.notifyTopicCreated();
        delegate.onTopicAdded(topicPath);
    }

    @Override
    public void onTopicAddFailed(String topicPath, TopicAddFailReason reason) {
        listenerNotifier.notifyTopicCreationFailed(reason);
        if (TopicAddFailReason.EXISTS.equals(reason)) {
            delegate.onTopicAdded(topicPath);
        }
        else {
            delegate.onTopicAddFailed(topicPath, reason);
        }
    }

    @Override
    public void onDiscard() {
        delegate.onDiscard();
    }
}
