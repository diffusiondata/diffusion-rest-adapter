package com.pushtechnology.adapters.rest.metrics.events;

import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.USER_CODE_ERROR;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.BINARY;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
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

import com.pushtechnology.adapters.rest.metrics.ITopicCreationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.ITopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.ITopicCreationSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Unit tests for {@link TopicCreationEventDispatcher}.
 *
 * @author Matt Champion 20/05/2017
 */
public final class TopicCreationEventDispatcherTest {
    @Mock
    private TopicCreationEventListener topicCreationEventListener;
    @Mock
    private Bytes bytes;
    @Captor
    private ArgumentCaptor<ITopicCreationRequestEvent> requestCaptor;
    @Captor
    private ArgumentCaptor<ITopicCreationSuccessEvent> successCaptor;
    @Captor
    private ArgumentCaptor<ITopicCreationFailedEvent> failedCaptor;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint")
        .url("endpoint")
        .topicPath("endpoint")
        .produces("string")
        .build();
    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service")
        .host("localhost")
        .port(80)
        .pollPeriod(5000)
        .topicPathRoot("service")
        .endpoints(singletonList(endpointConfig))
        .build();

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

        dispatcher.onTopicCreationRequest(serviceConfig, endpointConfig);

        verify(topicCreationEventListener).onTopicCreationRequest(requestCaptor.capture());
        final ITopicCreationRequestEvent value = requestCaptor.getValue();
        assertEquals("service/endpoint", value.getPath());
        assertEquals(BINARY, value.getTopicType());
        assertEquals(0, value.getInitialValueLength());
    }

    @Test
    public void onTopicCreationRequestWithData() throws Exception {
        final TopicCreationEventDispatcher dispatcher = new TopicCreationEventDispatcher(topicCreationEventListener);

        dispatcher.onTopicCreationRequest(serviceConfig, endpointConfig, bytes);

        verify(topicCreationEventListener).onTopicCreationRequest(requestCaptor.capture());
        final ITopicCreationRequestEvent value = requestCaptor.getValue();
        assertEquals("service/endpoint", value.getPath());
        assertEquals(BINARY, value.getTopicType());
        assertEquals(10, value.getInitialValueLength());
        verify(bytes).length();
    }

    @Test
    public void onTopicCreationSuccess() throws Exception {
        final TopicCreationEventDispatcher dispatcher = new TopicCreationEventDispatcher(topicCreationEventListener);

        dispatcher.onTopicCreationRequest(serviceConfig, endpointConfig).onTopicCreated();

        verify(topicCreationEventListener).onTopicCreationRequest(isA(TopicCreationRequestEvent.class));
        verify(topicCreationEventListener).onTopicCreationSuccess(successCaptor.capture());
        final ITopicCreationSuccessEvent value = successCaptor.getValue();
        assertEquals("service/endpoint", value.getRequestEvent().getPath());
        assertEquals(BINARY, value.getRequestEvent().getTopicType());
        assertEquals(0, value.getRequestEvent().getInitialValueLength());
    }

    @Test
    public void onTopicCreationFailure() throws Exception {
        final TopicCreationEventDispatcher dispatcher = new TopicCreationEventDispatcher(topicCreationEventListener);

        dispatcher.onTopicCreationRequest(serviceConfig, endpointConfig).onTopicCreationFailed(USER_CODE_ERROR);

        verify(topicCreationEventListener).onTopicCreationRequest(isA(TopicCreationRequestEvent.class));
        verify(topicCreationEventListener).onTopicCreationFailed(failedCaptor.capture());
        final ITopicCreationFailedEvent value = failedCaptor.getValue();
        assertEquals("service/endpoint", value.getRequestEvent().getPath());
        assertEquals(BINARY, value.getRequestEvent().getTopicType());
        assertEquals(0, value.getRequestEvent().getInitialValueLength());
        assertEquals(USER_CODE_ERROR, value.getFailReason());
    }
}
