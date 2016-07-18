package com.pushtechnology.adapters.rest.client;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link PollPublishingHandler}.
 *
 * @author Matt Champion on 18/07/2016
 */
public final class PollPublishingHandlerTest {
    @Mock
    private PublishingClient publishingClient;
    @Mock
    private JSON json;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint-0")
        .topic("topic")
        .url("http://localhost/json")
        .build();

    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .host("localhost")
        .port(8080)
        .pollPeriod(60000)
        .endpoints(singletonList(endpointConfig))
        .topicRoot("a")
        .build();

    private PollPublishingHandler pollHandler;

    @Before
    public void setUp() {
        initMocks(this);

        pollHandler = new PollPublishingHandler(publishingClient, serviceConfig, endpointConfig);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(publishingClient, json);
    }

    @Test
    public void completed() {
        pollHandler.completed(json);

        verify(publishingClient).publish(serviceConfig, endpointConfig, json);
    }

    @Test
    public void failed() {
        pollHandler.failed(new Exception("Intentional for test"));
    }

    @Test
    public void cancelled() {
        pollHandler.cancelled();
    }
}
