package com.pushtechnology.adapters.rest.metrics.event.listeners;

import static com.pushtechnology.diffusion.client.callbacks.ErrorReason.ACCESS_DENIED;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.After;
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

/**
 * Unit tests for {@link PublicationEventDispatcher}.
 *
 * @author Push Technology Limited
 */
public final class PublicationEventDispatcherTest {
    @Mock
    private PublicationEventListener publicationEventListener;
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

    @After
    public void postConditions() {
        verifyNoMoreInteractions(publicationEventListener);
    }

    @Test
    public void onPublicationRequest() throws Exception {
        final PublicationEventDispatcher dispatcher = new PublicationEventDispatcher(publicationEventListener);

        dispatcher.onPublicationRequest(serviceConfig, endpointConfig, 10);

        verify(publicationEventListener).onPublicationRequest(requestCaptor.capture());
    }

    @Test
    public void onPublication() throws Exception {
        final PublicationEventDispatcher dispatcher = new PublicationEventDispatcher(publicationEventListener);

        dispatcher.onPublicationRequest(serviceConfig, endpointConfig, 10).onPublication();

        final PublicationRequestEvent requestEvent =
            PublicationRequestEvent.Factory.create("service/endpoint", 10);
        verify(publicationEventListener).onPublicationRequest(requestCaptor.capture());
        verify(publicationEventListener).onPublicationSuccess(successCaptor.capture());
    }

    @Test
    public void onPublicationFailed() throws Exception {
        final PublicationEventDispatcher dispatcher = new PublicationEventDispatcher(publicationEventListener);

        dispatcher.onPublicationRequest(serviceConfig, endpointConfig, 10).onPublicationFailed(ACCESS_DENIED);

        final PublicationRequestEvent requestEvent =
            PublicationRequestEvent.Factory.create("service/endpoint", 10);
        verify(publicationEventListener).onPublicationRequest(requestCaptor.capture());
        verify(publicationEventListener).onPublicationFailed(failedCaptor.capture());
    }
}
