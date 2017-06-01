package com.pushtechnology.adapters.rest.metrics.event.listeners;

import static com.pushtechnology.diffusion.client.callbacks.ErrorReason.ACCESS_DENIED;
import static java.util.Collections.singletonList;
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

import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Unit tests for {@link PublicationEventDispatcher}.
 *
 * @author Matt Champion 20/05/2017
 */
public final class PublicationEventDispatcherTest {
    @Mock
    private PublicationEventListener publicationEventListener;
    @Mock
    private Bytes bytes;
    @Captor
    private ArgumentCaptor<PublicationRequestEvent> requestCaptor;
    @Captor
    private ArgumentCaptor<PublicationSuccessEvent> successCaptor;
    @Captor
    private ArgumentCaptor<PublicationFailedEvent> failedCaptor;

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
        verifyNoMoreInteractions(publicationEventListener, bytes);
    }

    @Test
    public void onPublicationRequest() throws Exception {
        final PublicationEventDispatcher dispatcher = new PublicationEventDispatcher(publicationEventListener);

        dispatcher.onPublicationRequest(serviceConfig, endpointConfig, bytes);

        verify(publicationEventListener).onPublicationRequest(requestCaptor.capture());
        verify(bytes).length();
    }

    @Test
    public void onPublication() throws Exception {
        final PublicationEventDispatcher dispatcher = new PublicationEventDispatcher(publicationEventListener);

        dispatcher.onPublicationRequest(serviceConfig, endpointConfig, bytes).onPublication();

        final PublicationRequestEvent requestEvent =
            PublicationRequestEvent.Factory.create("service/endpoint", 10);
        verify(publicationEventListener).onPublicationRequest(requestCaptor.capture());
        verify(publicationEventListener).onPublicationSuccess(successCaptor.capture());
        verify(bytes).length();
    }

    @Test
    public void onPublicationFailed() throws Exception {
        final PublicationEventDispatcher dispatcher = new PublicationEventDispatcher(publicationEventListener);

        dispatcher.onPublicationRequest(serviceConfig, endpointConfig, bytes).onPublicationFailed(ACCESS_DENIED);

        final PublicationRequestEvent requestEvent =
            PublicationRequestEvent.Factory.create("service/endpoint", 10);
        verify(publicationEventListener).onPublicationRequest(requestCaptor.capture());
        verify(publicationEventListener).onPublicationFailed(failedCaptor.capture());
        verify(bytes).length();
    }
}
