package com.pushtechnology.adapters;

import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.CompletableFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.publication.PublishingClientImpl;
import com.pushtechnology.adapters.rest.publication.UpdateTopicCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.UpdateSource;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.ValueUpdater;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link PublishingClientImpl}.
 *
 * @author Push Technology Limited
 */
public final class PublishingClientImplTest {
    @Mock
    private Session session;
    @Mock
    private Session.Listener listener;
    @Mock
    private TopicUpdateControl updateControl;
    @Mock
    private Updater rawUpdater;
    @Mock
    private ValueUpdater<JSON> updater;
    @Mock
    private JSON json;
    @Captor
    private ArgumentCaptor<UpdateSource> updateSourceCaptor;

    private EndpointConfig endpointConfig;
    private ServiceConfig serviceConfig;

    @Before
    public void setUp() {
        initMocks(this);

        when(session.feature(TopicUpdateControl.class)).thenReturn(updateControl);
        when(session.getState()).thenReturn(CONNECTED_ACTIVE);
        when(rawUpdater.valueUpdater(JSON.class)).thenReturn(updater);

        endpointConfig = EndpointConfig
            .builder()
            .name("endpoint-0")
            .topic("topic")
            .url("http://localhost/json")
            .build();

        serviceConfig = ServiceConfig
            .builder()
            .host("localhost")
            .port(8080)
            .pollPeriod(60000)
            .endpoints(singletonList(endpointConfig))
            .topicRoot("a")
            .build();
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(session, updateControl, rawUpdater, updater);
    }

    @Test
    public void start() {
        final PublishingClient client = new PublishingClientImpl(session);

        client.start();
    }

    @Test(expected = IllegalStateException.class)
    public void addServiceBeforeStart() {
        final PublishingClient client = new PublishingClientImpl(session);

        client.addService(serviceConfig);
    }

    @Test
    public void addService() {
        final PublishingClient client = new PublishingClientImpl(session);

        client.start();

        client.addService(serviceConfig);

        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());
    }

    @Test
    public void stopBeforeStart() {
        final PublishingClient client = new PublishingClientImpl(session);

        client.stop();
    }

    @Test
    public void stop() {
        final PublishingClient client = new PublishingClientImpl(session);

        client.start();

        client.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void publishBeforeStart() {
        final PublishingClient client = new PublishingClientImpl(session);

        client.publish(serviceConfig, endpointConfig, json);
    }

    @Test
    public void publish() {
        final PublishingClient client = new PublishingClientImpl(session);

        client.start();

        final CompletableFuture<ServiceConfig> promise = client.addService(serviceConfig);

        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        updateSourceCaptor.getValue().onActive("a/topic", rawUpdater);

        assertTrue(promise.isDone());

        client.publish(serviceConfig, endpointConfig, json);

        verify(session).getState();
        verify(rawUpdater).valueUpdater(JSON.class);
        verify(updater).update("a/topic", json, "a/topic", UpdateTopicCallback.INSTANCE);
    }
}
