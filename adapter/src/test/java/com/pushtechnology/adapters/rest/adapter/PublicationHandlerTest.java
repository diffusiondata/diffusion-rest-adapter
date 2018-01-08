package com.pushtechnology.adapters.rest.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.CancellationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.publication.UpdateContext;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link PublicationHandler}.
 *
 * @author Push Technology Limited
 */
public final class PublicationHandlerTest {
    @Mock
    private JSON json;
    @Mock
    private UpdateContext<JSON> updateContext;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint-0")
        .topicPath("topic")
        .url("http://localhost/json")
        .produces("json")
        .build();

    private PublicationHandler<JSON> pollHandler;

    @Before
    public void setUp() {
        initMocks(this);

        pollHandler = new PublicationHandler<>(endpointConfig, updateContext);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(updateContext, json);
    }

    @Test
    public void completed() {
        pollHandler.accept(json, null);

        verify(updateContext).publish(json);
    }

    @Test
    public void failed() {
        pollHandler.accept(null, new Exception("Intentional for test"));
    }

    @Test
    public void cancelled() {
        pollHandler.accept(null, new CancellationException());
    }
}
