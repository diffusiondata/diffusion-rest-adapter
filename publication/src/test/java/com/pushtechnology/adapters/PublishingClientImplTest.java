package com.pushtechnology.adapters;

import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.JSON;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.publication.AddTopicCallback;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.publication.PublishingClient.InitialiseCallback;
import com.pushtechnology.adapters.rest.publication.PublishingClientImpl;
import com.pushtechnology.adapters.rest.publication.UpdateTopicCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.UpdateSource;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.ValueUpdater;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link PublishingClientImpl}.
 *
 * @author Matt Champion on 13/07/2016
 */
public final class PublishingClientImplTest {
    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private TopicControl topicControl;
    @Mock
    private TopicUpdateControl updateControl;
    @Mock
    private Updater rawUpdater;
    @Mock
    private ValueUpdater<JSON> updater;
    @Mock
    private JSON json;
    @Mock
    private InitialiseCallback callback;
    @Captor
    private ArgumentCaptor<AddTopicCallback> addCallbackCaptor;
    @Captor
    private ArgumentCaptor<UpdateSource> updateSourceCaptor;

    private EndpointConfig endpointConfig;
    private ServiceConfig serviceConfig;

    @Before
    public void setUp() {
        initMocks(this);

        when(sessionFactory.listener(isA(Session.Listener.class))).thenReturn(sessionFactory);
        when(sessionFactory.open()).thenReturn(session);

        when(session.feature(TopicControl.class)).thenReturn(topicControl);
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
        verifyNoMoreInteractions(sessionFactory, session, topicControl, updateControl, rawUpdater, updater, callback);
    }

    @Test
    public void start() {
        final PublishingClient client = new PublishingClientImpl(sessionFactory);

        client.start();

        verify(sessionFactory).listener(isA(Session.Listener.class));
        verify(sessionFactory).open();
    }

    @Test(expected = IllegalStateException.class)
    public void initialiseBeforeStart() {
        final PublishingClient client = new PublishingClientImpl(sessionFactory);

        client.initialise(serviceConfig, callback);
    }

    @Test
    public void initialise() {
        final PublishingClient client = new PublishingClientImpl(sessionFactory);

        client.start();

        verify(sessionFactory).listener(isA(Session.Listener.class));
        verify(sessionFactory).open();

        client.initialise(serviceConfig, callback);

        verify(session).feature(TopicControl.class);
        verify(topicControl).addTopic(eq("a/topic"), eq(JSON), eq(endpointConfig), isA(AddTopicCallback.class));
    }

    @Test
    public void stopBeforeStart() {
        final PublishingClient client = new PublishingClientImpl(sessionFactory);

        client.stop();
    }

    @Test
    public void stop() {
        final PublishingClient client = new PublishingClientImpl(sessionFactory);

        client.start();

        verify(sessionFactory).listener(isA(Session.Listener.class));
        verify(sessionFactory).open();

        client.stop();

        verify(session).close();
    }

    @Test(expected = IllegalStateException.class)
    public void publishBeforeStart() {
        final PublishingClient client = new PublishingClientImpl(sessionFactory);

        client.publish(serviceConfig, endpointConfig, json);
    }

    @Test
    public void publish() {
        final PublishingClient client = new PublishingClientImpl(sessionFactory);

        client.start();

        verify(sessionFactory).listener(isA(Session.Listener.class));
        verify(sessionFactory).open();

        client.initialise(serviceConfig, callback);

        verify(session).feature(TopicControl.class);
        verify(topicControl).addTopic(eq("a/topic"), eq(JSON), eq(endpointConfig), addCallbackCaptor.capture());

        addCallbackCaptor.getValue().onTopicAdded(endpointConfig, "a/topic");
        verify(callback).onEndpointAdded(serviceConfig, endpointConfig);

        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        updateSourceCaptor.getValue().onActive("a/topic", rawUpdater);

        verify(callback).onServiceAdded(serviceConfig);

        client.publish(serviceConfig, endpointConfig, json);

        verify(session).getState();
        verify(session).feature(TopicUpdateControl.class);
        verify(rawUpdater).valueUpdater(JSON.class);
        verify(updater).update("a/topic", json, "a/topic", UpdateTopicCallback.INSTANCE);
    }
}
