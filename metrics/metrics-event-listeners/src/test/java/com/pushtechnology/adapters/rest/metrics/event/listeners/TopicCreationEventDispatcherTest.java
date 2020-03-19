/*******************************************************************************
 * Copyright (C) 2020 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.metrics.event.listeners;

import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.CLUSTER_REPARTITION;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.STRING;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.pushtechnology.adapters.rest.metrics.TopicCreationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationSuccessEvent;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Unit tests for {@link TopicCreationEventDispatcher}.
 *
 * @author Push Technology Limited
 */
public final class TopicCreationEventDispatcherTest {
    @Mock
    private TopicCreationEventListener topicCreationEventListener;
    @Mock
    private Bytes bytes;
    @Captor
    private ArgumentCaptor<TopicCreationRequestEvent> requestCaptor;
    @Captor
    private ArgumentCaptor<TopicCreationSuccessEvent> successCaptor;
    @Captor
    private ArgumentCaptor<TopicCreationFailedEvent> failedCaptor;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        when(bytes.length()).thenReturn(10);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(topicCreationEventListener, bytes);
    }

    @Test
    public void onTopicCreationRequest() throws Exception {
        final TopicCreationEventDispatcher dispatcher = new TopicCreationEventDispatcher(topicCreationEventListener);

        dispatcher.onTopicCreationRequest("service/endpoint", TopicType.STRING);

        verify(topicCreationEventListener).onTopicCreationRequest(requestCaptor.capture());
        final TopicCreationRequestEvent value = requestCaptor.getValue();
        assertEquals("service/endpoint", value.getPath());
        assertEquals(STRING, value.getTopicType());
    }

    @Test
    public void onTopicCreationSuccess() throws Exception {
        final TopicCreationEventDispatcher dispatcher = new TopicCreationEventDispatcher(topicCreationEventListener);

        dispatcher.onTopicCreationRequest("service/endpoint", TopicType.STRING).onTopicCreated();

        verify(topicCreationEventListener).onTopicCreationRequest(isA(TopicCreationRequestEvent.class));
        verify(topicCreationEventListener).onTopicCreationSuccess(successCaptor.capture());
        final TopicCreationSuccessEvent value = successCaptor.getValue();
        assertEquals("service/endpoint", value.getRequestEvent().getPath());
        assertEquals(STRING, value.getRequestEvent().getTopicType());
    }

    @Test
    public void onTopicCreationFailure() throws Exception {
        final TopicCreationEventDispatcher dispatcher = new TopicCreationEventDispatcher(topicCreationEventListener);

        dispatcher.onTopicCreationRequest("service/endpoint", TopicType.STRING).onTopicCreationFailed(CLUSTER_REPARTITION);

        verify(topicCreationEventListener).onTopicCreationRequest(isA(TopicCreationRequestEvent.class));
        verify(topicCreationEventListener).onTopicCreationFailed(failedCaptor.capture());
        final TopicCreationFailedEvent value = failedCaptor.getValue();
        assertEquals("service/endpoint", value.getRequestEvent().getPath());
        assertEquals(STRING, value.getRequestEvent().getTopicType());
        assertEquals(CLUSTER_REPARTITION, value.getFailReason());
    }
}
