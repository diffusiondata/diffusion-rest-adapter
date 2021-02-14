/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
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

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;

/**
 * Unit tests for {@link PollEventDispatcher}.
 *
 * @author Push Technology Limited
 */
public final class PollEventDispatcherTest {
    @Mock
    private PollEventListener pollEventListener;
    @Mock
    private EndpointResponse endpointResponse;
    @Captor
    private ArgumentCaptor<PollRequestEvent> requestCaptor;
    @Captor
    private ArgumentCaptor<PollSuccessEvent> successCaptor;
    @Captor
    private ArgumentCaptor<PollFailedEvent> failedCaptor;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint")
        .url("/endpoint")
        .topicPath("endpoint")
        .produces("string")
        .build();
    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service")
        .host("localhost")
        .pollPeriod(5000)
        .topicPathRoot("service")
        .endpoints(singletonList(endpointConfig))
        .build();

    @Before
    public void setUp() {
        when(endpointResponse.getStatusCode()).thenReturn(200);
        when(endpointResponse.getResponseLength()).thenReturn(10);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(pollEventListener, endpointResponse);
    }

    @Test
    public void onPollRequest() throws Exception {
        final PollEventDispatcher dispatcher = new PollEventDispatcher(pollEventListener);

        dispatcher.onPollRequest(serviceConfig, endpointConfig);

        verify(pollEventListener).onPollRequest(requestCaptor.capture());
        final PollRequestEvent value = requestCaptor.getValue();
        assertEquals("https://localhost:443/endpoint", value.getUri());
    }

    @Test
    public void onPollResponse() throws Exception {
        final PollEventDispatcher dispatcher = new PollEventDispatcher(pollEventListener);

        dispatcher.onPollRequest(serviceConfig, endpointConfig).onPollResponse(endpointResponse);

        verify(pollEventListener).onPollRequest(requestCaptor.capture());
        verify(pollEventListener).onPollSuccess(successCaptor.capture());
        verify(endpointResponse).getStatusCode();
        verify(endpointResponse).getResponseLength();
        final PollSuccessEvent value = successCaptor.getValue();
        assertEquals(200, value.getStatusCode());
        assertEquals(10, value.getResponseLength());
    }

    @Test
    public void onPollFailure() throws Exception {
        final PollEventDispatcher dispatcher = new PollEventDispatcher(pollEventListener);

        final Exception exception = new Exception("for test");
        dispatcher.onPollRequest(serviceConfig, endpointConfig).onPollFailure(exception);

        verify(pollEventListener).onPollRequest(requestCaptor.capture());
        verify(pollEventListener).onPollFailed(failedCaptor.capture());
        final PollFailedEvent value = failedCaptor.getValue();
        assertEquals(exception, value.getException());
    }
}
