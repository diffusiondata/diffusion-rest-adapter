package com.pushtechnology.adapters.rest.publication;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener.PublicationCompletionListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Unit tests for {@link ListenerNotifierImpl}.
 *
 * @author Push Technology Limited
 */
public final class ListenerNotifierTest {
    @Mock
    private PublicationListener publicationListener;
    @Mock
    private PublicationCompletionListener completionListener;
    @Mock
    private Bytes bytes;

    private EndpointConfig endpointConfig;
    private ServiceConfig serviceConfig;

    @Before
    public void setUp() {
        initMocks(this);

        endpointConfig = EndpointConfig
            .builder()
            .name("endpoint-0")
            .topicPath("topic")
            .url("http://localhost/json")
            .produces("json")
            .build();

        serviceConfig = ServiceConfig
            .builder()
            .name("service")
            .host("localhost")
            .port(8080)
            .pollPeriod(60000)
            .endpoints(singletonList(endpointConfig))
            .topicPathRoot("a")
            .build();

        when(publicationListener.onPublicationRequest(serviceConfig, endpointConfig, bytes))
            .thenReturn(completionListener);
    }

    @Test
    public void notifyPublicationRequest() throws Exception {
        final ListenerNotifierImpl notifier = new ListenerNotifierImpl(publicationListener, serviceConfig, endpointConfig);

        final PublicationCompletionListener listener = notifier.notifyPublicationRequest(bytes);

        assertEquals(completionListener, listener);
        verify(publicationListener).onPublicationRequest(serviceConfig, endpointConfig, bytes);
    }
}
