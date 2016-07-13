package com.pushtechnology.adapters;

import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.EXISTS;
import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.EXISTS_MISMATCH;
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
import com.pushtechnology.adapters.rest.publication.AddTopicCallback;
import com.pushtechnology.adapters.rest.publication.PublishingClient;

/**
 * Unit tests for {@link AddTopicCallback}.
 *
 * @author Matt Champion on 13/07/2016
 */
public final class AddTopicCallbackTest {
    @Mock
    private PublishingClient.InitialiseCallback callback;

    private ServiceConfig serviceConfig;
    private EndpointConfig endpointConfig;
    private AddTopicCallback addTopicCallback;

    @Before
    public void setUp() {
        initMocks(this);

        endpointConfig = EndpointConfig
            .builder()
            .name("endpoint")
            .topic("topic")
            .url("http://localhost/json")
            .build();

        serviceConfig = ServiceConfig
            .builder()
            .host("localhost")
            .port(8080)
            .pollPeriod(60000)
            .endpoints(singletonList(endpointConfig))
            .build();

        addTopicCallback = new AddTopicCallback(serviceConfig, callback);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void onTopicAdded() {
        addTopicCallback.onTopicAdded(endpointConfig, "topic");

        verify(callback).onEndpointAdded(serviceConfig, endpointConfig);
        verify(callback).onServiceAdded(serviceConfig);
    }

    @Test
    public void onTopicAddFailedExists() {
        addTopicCallback.onTopicAddFailed(endpointConfig, "topic", EXISTS);

        verify(callback).onEndpointAdded(serviceConfig, endpointConfig);
        verify(callback).onServiceAdded(serviceConfig);
    }

    @Test
    public void onTopicAddFailed() {
        addTopicCallback.onTopicAddFailed(endpointConfig, "topic", EXISTS_MISMATCH);
        verify(callback).onEndpointFailed(serviceConfig, endpointConfig);
        verify(callback).onServiceAdded(serviceConfig);
    }

    @Test
    public void onDiscard() {
        addTopicCallback.onDiscard(endpointConfig);
        verify(callback).onEndpointFailed(serviceConfig, endpointConfig);
        verify(callback).onServiceAdded(serviceConfig);
    }
}
