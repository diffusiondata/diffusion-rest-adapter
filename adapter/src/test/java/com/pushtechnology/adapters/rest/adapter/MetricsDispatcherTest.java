package com.pushtechnology.adapters.rest.adapter;

import static com.pushtechnology.diffusion.client.callbacks.ErrorReason.ACCESS_DENIED;
import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.INVALID_DETAILS;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.pushtechnology.adapters.rest.metrics.listeners.PollListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PollListener.PollCompletionListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener.PublicationCompletionListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener.TopicCreationCompletionListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Unit tests for {@link MetricsDispatcher}.
 *
 * @author Matt Champion 10/07/2017
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
    private Bytes bytes;
    @Mock
    private HttpResponse httpResponse;

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
        when(publicationListener0.onPublicationRequest(any(), any(), any())).thenReturn(publicationCompletionListener0);
        when(publicationListener1.onPublicationRequest(any(), any(), any())).thenReturn(publicationCompletionListener1);
        when(topicCreationListener0.onTopicCreationRequest(any(), any())).thenReturn(topicCreationCompletionListener0);
        when(topicCreationListener1.onTopicCreationRequest(any(), any())).thenReturn(topicCreationCompletionListener1);
        when(topicCreationListener0.onTopicCreationRequest(any(), any(), any())).thenReturn(topicCreationCompletionListener0);
        when(topicCreationListener1.onTopicCreationRequest(any(), any(), any())).thenReturn(topicCreationCompletionListener1);
    }

    @Test
    public void pollListenerDispatch() {
        final MetricsDispatcher dispatcher = new MetricsDispatcher();

        dispatcher.addPollListener(pollListener0);
        dispatcher.addPollListener(pollListener1);

        final PollCompletionListener completionListener = dispatcher.onPollRequest(serviceConfig, endpointConfig);

        verify(pollListener0).onPollRequest(serviceConfig, endpointConfig);
        verify(pollListener1).onPollRequest(serviceConfig, endpointConfig);

        completionListener.onPollResponse(httpResponse);

        verify(pollCompletionListener0).onPollResponse(httpResponse);
        verify(pollCompletionListener1).onPollResponse(httpResponse);

        final Exception e = new Exception("");
        completionListener.onPollFailure(e);

        verify(pollCompletionListener0).onPollFailure(e);
        verify(pollCompletionListener1).onPollFailure(e);
    }

    @Test
    public void publicationListenerDispatch() {
        final MetricsDispatcher dispatcher = new MetricsDispatcher();

        dispatcher.addPublicationListener(publicationListener0);
        dispatcher.addPublicationListener(publicationListener1);

        final PublicationCompletionListener completionListener = dispatcher.onPublicationRequest(serviceConfig, endpointConfig, bytes);

        verify(publicationListener0).onPublicationRequest(serviceConfig, endpointConfig, bytes);
        verify(publicationListener1).onPublicationRequest(serviceConfig, endpointConfig, bytes);

        completionListener.onPublication();

        verify(publicationCompletionListener0).onPublication();
        verify(publicationCompletionListener1).onPublication();

        completionListener.onPublicationFailed(ACCESS_DENIED);

        verify(publicationCompletionListener0).onPublicationFailed(ACCESS_DENIED);
        verify(publicationCompletionListener1).onPublicationFailed(ACCESS_DENIED);
    }

    @Test
    public void topicCreationListenerDispatch() {
        final MetricsDispatcher dispatcher = new MetricsDispatcher();

        dispatcher.addTopicCreationListener(topicCreationListener0);
        dispatcher.addTopicCreationListener(topicCreationListener1);

        final TopicCreationCompletionListener completionListener = dispatcher.onTopicCreationRequest(serviceConfig, endpointConfig);

        verify(topicCreationListener0).onTopicCreationRequest(serviceConfig, endpointConfig);
        verify(topicCreationListener1).onTopicCreationRequest(serviceConfig, endpointConfig);

        completionListener.onTopicCreated();

        verify(topicCreationCompletionListener0).onTopicCreated();
        verify(topicCreationCompletionListener1).onTopicCreated();

        completionListener.onTopicCreationFailed(INVALID_DETAILS);

        verify(topicCreationCompletionListener0).onTopicCreationFailed(INVALID_DETAILS);
        verify(topicCreationCompletionListener1).onTopicCreationFailed(INVALID_DETAILS);
    }

    @Test
    public void topicCreationListenerWithValueDispatch() {
        final MetricsDispatcher dispatcher = new MetricsDispatcher();

        dispatcher.addTopicCreationListener(topicCreationListener0);
        dispatcher.addTopicCreationListener(topicCreationListener1);

        final TopicCreationCompletionListener completionListener = dispatcher.onTopicCreationRequest(serviceConfig, endpointConfig, bytes);

        verify(topicCreationListener0).onTopicCreationRequest(serviceConfig, endpointConfig, bytes);
        verify(topicCreationListener1).onTopicCreationRequest(serviceConfig, endpointConfig, bytes);

        completionListener.onTopicCreated();

        verify(topicCreationCompletionListener0).onTopicCreated();
        verify(topicCreationCompletionListener1).onTopicCreated();

        completionListener.onTopicCreationFailed(INVALID_DETAILS);

        verify(topicCreationCompletionListener0).onTopicCreationFailed(INVALID_DETAILS);
        verify(topicCreationCompletionListener1).onTopicCreationFailed(INVALID_DETAILS);
    }
}
