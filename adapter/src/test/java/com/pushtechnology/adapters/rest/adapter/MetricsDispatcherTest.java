package com.pushtechnology.adapters.rest.adapter;

import static com.pushtechnology.diffusion.client.callbacks.ErrorReason.ACCESS_DENIED;
import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.INVALID_DETAILS;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.pushtechnology.adapters.rest.metrics.event.listeners.PollEventListener;
import com.pushtechnology.adapters.rest.metrics.event.listeners.PublicationEventListener;
import com.pushtechnology.adapters.rest.metrics.event.listeners.TopicCreationEventListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PollListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PollListener.PollCompletionListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener.PublicationCompletionListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener.TopicCreationCompletionListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.datatype.Bytes;

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
    private Bytes bytes;
    @Mock
    private HttpResponse httpResponse;
    @Mock
    private StatusLine statusLine;
    @Mock
    private HttpEntity entity;

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
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getEntity()).thenReturn(entity);
    }

    @Test
    public void pollEventListenerDispatch() {
        final MetricsDispatcher dispatcher = new MetricsDispatcher();

        dispatcher.addPollEventListener(pollEventListener0);
        dispatcher.addPollEventListener(pollEventListener1);

        final PollCompletionListener completionListener = dispatcher.onPollRequest(serviceConfig, endpointConfig);

        verify(pollEventListener0).onPollRequest(isNotNull());
        verify(pollEventListener1).onPollRequest(isNotNull());

        completionListener.onPollResponse(httpResponse);

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

        final PublicationCompletionListener completionListener = dispatcher.onPublicationRequest(serviceConfig, endpointConfig, bytes);

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

        final TopicCreationCompletionListener completionListener = dispatcher.onTopicCreationRequest(serviceConfig, endpointConfig);

        verify(topicCreationEventListener0).onTopicCreationRequest(isNotNull());
        verify(topicCreationEventListener1).onTopicCreationRequest(isNotNull());

        completionListener.onTopicCreated();

        verify(topicCreationEventListener0).onTopicCreationSuccess(isNotNull());
        verify(topicCreationEventListener1).onTopicCreationSuccess(isNotNull());

        completionListener.onTopicCreationFailed(INVALID_DETAILS);

        verify(topicCreationEventListener0).onTopicCreationFailed(isNotNull());
        verify(topicCreationEventListener1).onTopicCreationFailed(isNotNull());
    }
}
