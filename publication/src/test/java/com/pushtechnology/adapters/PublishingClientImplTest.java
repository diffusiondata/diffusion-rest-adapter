package com.pushtechnology.adapters;

import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_FAILED;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.RECOVERING_RECONNECT;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.isA;
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
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.callbacks.Registration;
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
    private Registration registration;
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
        verifyNoMoreInteractions(session, updateControl, rawUpdater, updater, registration);
    }

    @Test
    public void addService() {
        final PublishingClient client = new PublishingClientImpl(session);

        client.addService(serviceConfig);

        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), isA(UpdateSource.class));
    }

    @Test
    public void addServiceStandby() {
        final PublishingClient client = new PublishingClientImpl(session);

        client.addService(serviceConfig);

        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        final UpdateSource value = updateSourceCaptor.getValue();

        value.onStandby("a");
    }

    @Test
    public void publishSuccess() {
        final PublishingClient client = new PublishingClientImpl(session);

        final CompletableFuture<ServiceConfig> promise = client.addService(serviceConfig);

        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        updateSourceCaptor.getValue().onActive("a/topic", rawUpdater);

        assertTrue(promise.isDone());

        client.publish(serviceConfig, endpointConfig, json);

        verify(session).getState();
        verify(rawUpdater).valueUpdater(JSON.class);
        verify(updater).update("a/topic", json, "a/topic", UpdateTopicCallback.INSTANCE);

        UpdateTopicCallback.INSTANCE.onSuccess("a/topic");
    }

    @Test
    public void publishFailure() {
        final PublishingClient client = new PublishingClientImpl(session);

        final CompletableFuture<ServiceConfig> promise = client.addService(serviceConfig);

        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        updateSourceCaptor.getValue().onActive("a/topic", rawUpdater);

        assertTrue(promise.isDone());

        client.publish(serviceConfig, endpointConfig, json);

        verify(session).getState();
        verify(rawUpdater).valueUpdater(JSON.class);
        verify(updater).update("a/topic", json, "a/topic", UpdateTopicCallback.INSTANCE);

        UpdateTopicCallback.INSTANCE.onError("a/topic", ErrorReason.COMMUNICATION_FAILURE);
    }

    @Test
    public void publishWhenServiceNotAdded() {
        final PublishingClient client = new PublishingClientImpl(session);

        client.publish(serviceConfig, endpointConfig, json);

        verify(session).getState();
    }

    @Test
    public void publishWhenRecovering() {
        when(session.getState()).thenReturn(RECOVERING_RECONNECT);

        final PublishingClient client = new PublishingClientImpl(session);

        client.publish(serviceConfig, endpointConfig, json);

        verify(session).getState();
    }

    @Test(expected = IllegalStateException.class)
    public void publishWhenClosed() {
        when(session.getState()).thenReturn(CLOSED_FAILED);

        final PublishingClient client = new PublishingClientImpl(session);

        try {
            client.publish(serviceConfig, endpointConfig, json);
        }
        finally {
            verify(session).getState();
        }
    }

    @Test
    public void removeService() {
        final PublishingClient client = new PublishingClientImpl(session);

        client.addService(serviceConfig);

        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        final UpdateSource updateSource = updateSourceCaptor.getValue();

        updateSource.onRegistered("a", registration);

        client.removeService(serviceConfig);

        verify(registration).close();
    }

    @Test
    public void removeUnknownService() {
        final PublishingClient client = new PublishingClientImpl(session);

        client.removeService(serviceConfig);
    }
}
