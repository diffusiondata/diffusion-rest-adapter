package com.pushtechnology.adapters.rest.metrics.events;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
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

/**
 * Unit tests for {@link PollEventDispatcher}.
 *
 * @author Matt Champion 20/05/2017
 */
public final class PollEventDispatcherTest {
    @Mock
    private PollEventListener pollEventListener;
    @Mock
    private HttpResponse httpResponse;
    @Mock
    private StatusLine statusLine;
    @Mock
    private HttpEntity entity;
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
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getEntity()).thenReturn(entity);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(entity.getContentLength()).thenReturn(10L);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(pollEventListener, httpResponse);
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

        dispatcher.onPollRequest(serviceConfig, endpointConfig).onPollResponse(httpResponse);

        verify(pollEventListener).onPollRequest(requestCaptor.capture());
        verify(pollEventListener).onPollSuccess(successCaptor.capture());
        verify(httpResponse).getEntity();
        verify(httpResponse).getStatusLine();
        verify(statusLine).getStatusCode();
        verify(entity).getContentLength();
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