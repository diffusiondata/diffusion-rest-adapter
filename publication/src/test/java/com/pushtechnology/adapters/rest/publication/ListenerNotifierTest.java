package com.pushtechnology.adapters.rest.publication;

import static com.pushtechnology.diffusion.client.callbacks.ErrorReason.ACCESS_DENIED;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.metrics.PublicationListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Unit tests for {@link ListenerNotifierImpl}.
 *
 * @author Matt Champion 13/05/2017
 */
public final class ListenerNotifierTest {
    @Mock
    private PublicationListener publicationListener;
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
    }

    @Test
    public void notifyPublicationRequest() throws Exception {
        final ListenerNotifierImpl notifier = new ListenerNotifierImpl(publicationListener, serviceConfig, endpointConfig);

        notifier.notifyPublicationRequest(bytes);

        verify(publicationListener).onPublicationRequest(serviceConfig, endpointConfig, bytes);
    }

    @Test
    public void notifyPublication() throws Exception {
        final ListenerNotifierImpl notifier = new ListenerNotifierImpl(publicationListener, serviceConfig, endpointConfig);

        notifier.notifyPublication(bytes);

        verify(publicationListener).onPublication(serviceConfig, endpointConfig, bytes);
    }

    @Test
    public void notifyPublicationFailed() throws Exception {
        final ListenerNotifierImpl notifier = new ListenerNotifierImpl(publicationListener, serviceConfig, endpointConfig);

        notifier.notifyPublicationFailed(bytes, ACCESS_DENIED);

        verify(publicationListener).onPublicationFailed(serviceConfig, endpointConfig, bytes, ACCESS_DENIED);
    }
}
