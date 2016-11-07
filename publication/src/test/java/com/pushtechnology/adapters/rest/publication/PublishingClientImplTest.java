/*******************************************************************************
 * Copyright (C) 2016 Push Technology Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.pushtechnology.adapters.rest.publication;

import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.RECOVERING_RECONNECT;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.BINARY;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.JSON;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import com.pushtechnology.adapters.rest.session.management.EventedSessionListener;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.callbacks.Registration;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.UpdateSource;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.ValueUpdater;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.binary.Binary;
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
    private TopicUpdateControl updateControl;
    @Mock
    private Registration registration;
    @Mock
    private Updater rawUpdater;
    @Mock
    private ValueUpdater<JSON> jsonUpdater;
    @Mock
    private ValueUpdater<Binary> binaryUpdater;
    @Mock
    private JSON json;
    @Mock
    private Binary binary;
    @Captor
    private ArgumentCaptor<UpdateSource> updateSourceCaptor;

    private Session.Listener sessionListener;
    private EventedSessionListener eventedListener;
    private EndpointConfig endpointConfig;
    private ServiceConfig serviceConfig;

    private PublishingClient client;

    @Before
    public void setUp() {
        initMocks(this);

        when(session.feature(TopicUpdateControl.class)).thenReturn(updateControl);
        when(session.getState()).thenReturn(CONNECTED_ACTIVE);
        when(rawUpdater.valueUpdater(JSON.class)).thenReturn(jsonUpdater);
        when(rawUpdater.valueUpdater(Binary.class)).thenReturn(binaryUpdater);

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

        eventedListener = new EventedSessionListener();

        final SessionFactory factory = mock(SessionFactory.class);
        eventedListener.addTo(factory);

        final ArgumentCaptor<Session.Listener> captor = new ArgumentCaptor<>();
        verify(factory).listener(captor.capture());
        sessionListener = captor.getValue();

        client = new PublishingClientImpl(session, eventedListener);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(session, updateControl, rawUpdater, binaryUpdater, registration);
    }

    @Test
    public void addService() {
        client.addService(serviceConfig);

        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), isA(UpdateSource.class));
    }

    @Test
    public void addServiceStandby() {
        client.addService(serviceConfig);

        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        final UpdateSource updateSource = updateSourceCaptor.getValue();
        updateSource.onRegistered("a", registration);

        updateSource.onStandby("a");
    }

    @Test
    public void removeService() {
        client.addService(serviceConfig);

        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        final UpdateSource updateSource = updateSourceCaptor.getValue();

        updateSource.onRegistered("a", registration);
        updateSource.onStandby("a");

        final CompletableFuture<?> future = client.removeService(serviceConfig);
        assertFalse(future.isDone());

        verify(registration).close();
        updateSource.onClose("a");
        assertTrue(future.isDone());
    }

    @Test
    public void failToRegister() {
        client.addService(serviceConfig);

        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        final UpdateSource updateSource = updateSourceCaptor.getValue();

        updateSource.onError("a", ErrorReason.COMMUNICATION_FAILURE);
    }

    @Test
    public void removeUnknownService() {
        final CompletableFuture<?> future = client.removeService(serviceConfig);

        assertTrue(future.isDone());
    }

    @Test
    public void jsonContext() {
        final EventedUpdateSource source = client.addService(serviceConfig);
        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        updateSourceCaptor.getValue().onActive("a/topic", rawUpdater);
        verify(rawUpdater).valueUpdater(JSON.class);
        verify(rawUpdater).valueUpdater(Binary.class);

        final UpdateContext<JSON> updateContext = client.createUpdateContext(serviceConfig, endpointConfig, JSON);

        updateContext.publish(json);

        verify(session).getState();
        verify(jsonUpdater).update("a/topic", json, "a/topic", UpdateTopicCallback.INSTANCE);

        UpdateTopicCallback.INSTANCE.onSuccess("a/topic");
    }

    @Test
    public void binaryContext() {
        final EventedUpdateSource source = client.addService(serviceConfig);
        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        updateSourceCaptor.getValue().onActive("a/topic", rawUpdater);
        verify(rawUpdater).valueUpdater(JSON.class);
        verify(rawUpdater).valueUpdater(Binary.class);

        final UpdateContext<Binary> updateContext = client.createUpdateContext(serviceConfig, endpointConfig, BINARY);

        updateContext.publish(binary);

        verify(session).getState();
        verify(binaryUpdater).update("a/topic", binary, "a/topic", UpdateTopicCallback.INSTANCE);

        UpdateTopicCallback.INSTANCE.onSuccess("a/topic");
    }

    @Test(expected = IllegalArgumentException.class)
    public void singleValueContext() {
        final EventedUpdateSource source = client.addService(serviceConfig);
        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        updateSourceCaptor.getValue().onActive("a/topic", rawUpdater);
        verify(rawUpdater).valueUpdater(JSON.class);
        verify(rawUpdater).valueUpdater(Binary.class);

        client.createUpdateContext(serviceConfig, endpointConfig, TopicType.SINGLE_VALUE);
    }

    @Test(expected = IllegalStateException.class)
    public void noUpdater() {
        client.createUpdateContext(serviceConfig, endpointConfig, JSON);
    }

    @Test
    public void contextRecovery() {
        final EventedUpdateSource source = client.addService(serviceConfig);
        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        updateSourceCaptor.getValue().onActive("a/topic", rawUpdater);
        verify(rawUpdater).valueUpdater(JSON.class);
        verify(rawUpdater).valueUpdater(Binary.class);

        when(session.getState()).thenReturn(RECOVERING_RECONNECT);

        final UpdateContext<JSON> updateContext = client.createUpdateContext(serviceConfig, endpointConfig, JSON);

        updateContext.publish(json);

        verify(session).getState();
        verify(jsonUpdater, never()).update("a/topic", json, "a/topic", UpdateTopicCallback.INSTANCE);

        sessionListener.onSessionStateChanged(session, RECOVERING_RECONNECT, CONNECTED_ACTIVE);

        verify(jsonUpdater).update("a/topic", json, "a/topic", UpdateTopicCallback.INSTANCE);
    }
}
