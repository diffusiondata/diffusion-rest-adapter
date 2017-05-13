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

package com.pushtechnology.adapters.rest.topic.management;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;

/**
 * Unit tests for {@link TopicSetupCallback}.
 *
 * @author Push Technology Limited
 */
public final class TopicSetupCallbackTest {
    @Mock
    private TopicControl.AddCallback delegate;
    @Mock
    private ListenerNotifier listenerNotifier;

    private TopicControl.AddCallback callback;

    @Before
    public void setUp() {
        initMocks(this);

        callback = new TopicSetupCallback(listenerNotifier, delegate);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(delegate, listenerNotifier);
    }

    @Test
    public void onTopicAdded() {
        callback.onTopicAdded("a/topic");

        verify(delegate).onTopicAdded("a/topic");
        verify(listenerNotifier).notifyTopicCreated();
    }

    @Test
    public void onTopicAddFailed() {
        callback.onTopicAddFailed("a/topic", TopicAddFailReason.TOPIC_NOT_FOUND);

        verify(delegate).onTopicAddFailed("a/topic", TopicAddFailReason.TOPIC_NOT_FOUND);
        verify(listenerNotifier).notifyTopicCreationFailed(TopicAddFailReason.TOPIC_NOT_FOUND);
    }

    @Test
    public void onDiscard() {
        callback.onDiscard();

        verify(delegate).onDiscard();
    }

    @Test
    public void topicAlreadyPresent() {
        callback.onTopicAddFailed("a/topic", TopicAddFailReason.EXISTS);

        verify(delegate).onTopicAdded("a/topic");
        verify(listenerNotifier).notifyTopicCreationFailed(TopicAddFailReason.EXISTS);
    }
}
