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

package com.pushtechnology.adapters.rest.adapter;

import static com.pushtechnology.diffusion.client.callbacks.ErrorReason.ACCESS_DENIED;
import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.INVALID_DETAILS;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.pushtechnology.adapters.rest.metrics.event.listeners.PollEventListener;
import com.pushtechnology.adapters.rest.metrics.event.listeners.PublicationEventListener;
import com.pushtechnology.adapters.rest.metrics.event.listeners.ServiceEventListener;
import com.pushtechnology.adapters.rest.metrics.event.listeners.TopicCreationEventListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PollListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PollListener.PollCompletionListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener.PublicationCompletionListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener.TopicCreationCompletionListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.diffusion.client.topics.details.TopicType;

/**
 * Unit tests for {@link MetricsDispatcher}.
 *
 * @author Push Technology Limited
 */
public final class MetricsDispatcherTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private PollListener pollListener0;
    @Mock
    private PollListener pollListener1;
    @Mock
    private PublicationListener publicationListener0;
    @Mock
    private PublicationListener publicationListener1;
    @Mock
    private TopicCreationListener topicCreationListener0;
    @Mock
    private TopicCreationListener topicCreationListener1;
    @Mock
    private PollCompletionListener pollCompletionListener0;
    @Mock
    private PollCompletionListener pollCompletionListener1;
    @Mock
    private PublicationCompletionListener publicationCompletionListener0;
    @Mock
    private PublicationCompletionListener publicationCompletionListener1;
    @Mock
    private TopicCreationCompletionListener topicCreationCompletionListener0;
    @Mock
    private TopicCreationCompletionListener topicCreationCompletionListener1;
    @Mock
    private PollEventListener pollEventListener0;
    @Mock
    private PublicationEventListener publicationEventListener0;
    @Mock
    private TopicCreationEventListener topicCreationEventListener0;
    @Mock
    private PollEventListener pollEventListener1;
    @Mock
    private PublicationEventListener publicationEventListener1;
    @Mock
    private TopicCreationEventListener topicCreationEventListener1;
    @Mock
    private EndpointResponse endpointResponse;
    @Mock
    private ServiceEventListener serviceEventListener0;
    @Mock
    private ServiceEventListener serviceEventListener1;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint-0")
        .topicPath("path")
        .url("/a/url/json")
        .produces("json")
        .build();
    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service")
        .host("localhost")
        .port(80)
        .pollPeriod(5000L)
        .topicPathRoot("topic")
        .endpoints(singletonList(endpointConfig))
        .build();

    @Before
    public void setUp() {
        when(pollListener0.onPollRequest(any(), any())).thenReturn(pollCompletionListener0);
        when(pollListener1.onPollRequest(any(), any())).thenReturn(pollCompletionListener1);
        when(publicationListener0.onPublicationRequest(any(), anyInt())).thenReturn(publicationCompletionListener0);
        when(publicationListener1.onPublicationRequest(any(), anyInt())).thenReturn(publicationCompletionListener1);
        when(topicCreationListener0.onTopicCreationRequest(any(), any())).thenReturn(topicCreationCompletionListener0);
        when(topicCreationListener1.onTopicCreationRequest(any(), any())).thenReturn(topicCreationCompletionListener1);
    }

    @Test
    public void pollEventListenerDispatch() {
        final MetricsDispatcher dispatcher = new MetricsDispatcher();

        dispatcher.addPollEventListener(pollEventListener0);
        dispatcher.addPollEventListener(pollEventListener1);

        final PollCompletionListener completionListener = dispatcher.onPollRequest(serviceConfig, endpointConfig);

        verify(pollEventListener0).onPollRequest(isNotNull());
        verify(pollEventListener1).onPollRequest(isNotNull());

        completionListener.onPollResponse(endpointResponse);

        verify(pollEventListener0).onPollSuccess(isNotNull());
        verify(pollEventListener1).onPollSuccess(isNotNull());

        final Exception e = new Exception("");
        completionListener.onPollFailure(e);

        verify(pollEventListener0).onPollFailed(isNotNull());
        verify(pollEventListener1).onPollFailed(isNotNull());
    }

    @Test
    public void publicationEventListenerDispatch() {
        final MetricsDispatcher dispatcher = new MetricsDispatcher();

        dispatcher.addPublicationEventListener(publicationEventListener0);
        dispatcher.addPublicationEventListener(publicationEventListener1);

        final PublicationCompletionListener completionListener = dispatcher.onPublicationRequest("topic/path", 10);

        verify(publicationEventListener0).onPublicationRequest(isNotNull());
        verify(publicationEventListener1).onPublicationRequest(isNotNull());

        completionListener.onPublication();

        verify(publicationEventListener0).onPublicationSuccess(isNotNull());
        verify(publicationEventListener1).onPublicationSuccess(isNotNull());

        completionListener.onPublicationFailed(ACCESS_DENIED);

        verify(publicationEventListener0).onPublicationFailed(isNotNull());
        verify(publicationEventListener1).onPublicationFailed(isNotNull());
    }

    @Test
    public void topicCreationEventListenerDispatch() {
        final MetricsDispatcher dispatcher = new MetricsDispatcher();

        dispatcher.addTopicCreationEventListener(topicCreationEventListener0);
        dispatcher.addTopicCreationEventListener(topicCreationEventListener1);

        final TopicCreationCompletionListener completionListener = dispatcher.onTopicCreationRequest("topic/a/url/json", TopicType.JSON);

        verify(topicCreationEventListener0).onTopicCreationRequest(isNotNull());
        verify(topicCreationEventListener1).onTopicCreationRequest(isNotNull());

        completionListener.onTopicCreated();

        verify(topicCreationEventListener0).onTopicCreationSuccess(isNotNull());
        verify(topicCreationEventListener1).onTopicCreationSuccess(isNotNull());

        completionListener.onTopicCreationFailed(INVALID_DETAILS);

        verify(topicCreationEventListener0).onTopicCreationFailed(isNotNull());
        verify(topicCreationEventListener1).onTopicCreationFailed(isNotNull());
    }

    @Test
    public void serviceEventListenerDispatch() {
        final MetricsDispatcher dispatcher = new MetricsDispatcher();

        dispatcher.addServiceEventListener(serviceEventListener0);
        dispatcher.addServiceEventListener(serviceEventListener1);

        dispatcher.onStandby(serviceConfig);

        verify(serviceEventListener0).onStandby(serviceConfig);
        verify(serviceEventListener1).onStandby(serviceConfig);

        dispatcher.onActive(serviceConfig);

        verify(serviceEventListener0).onActive(serviceConfig);
        verify(serviceEventListener1).onActive(serviceConfig);

        dispatcher.onRemove(serviceConfig);

        verify(serviceEventListener0).onRemove(serviceConfig);
        verify(serviceEventListener1).onRemove(serviceConfig);
    }
}
