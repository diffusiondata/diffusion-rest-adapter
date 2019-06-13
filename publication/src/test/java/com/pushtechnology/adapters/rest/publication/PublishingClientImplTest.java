/*******************************************************************************
 * Copyright (C) 2019 Push Technology Ltd.
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

import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.session.management.EventedSessionListener;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.callbacks.Registration;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.UpdateSource;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater.UpdateContextCallback;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;
import com.pushtechnology.diffusion.datatype.DataType;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link PublishingClientImpl}.
 *
 * @author Push Technology Limited
 */
@SuppressWarnings("deprecation")
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
    private JSON json;
    @Mock
    private Binary binary;
    @Mock
    private PublicationListener publicationListener;
    @Mock
    private DataType<Binary> binaryDataType;
    @Mock
    private DataType<JSON> jsonDataType;
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
        when(binaryDataType.toBytes(binary)).thenReturn(binary);
        when(jsonDataType.toBytes(json)).thenReturn(json);
        when(updateControl.updater()).thenReturn(rawUpdater);
        when(jsonDataType.getTypeName()).thenReturn("json");
        when(binaryDataType.getTypeName()).thenReturn("binary");

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

        final ArgumentCaptor<Session.Listener> captor = ArgumentCaptor.forClass(Session.Listener.class);
        verify(factory).listener(captor.capture());
        sessionListener = captor.getValue();

        client = new PublishingClientImpl(session, eventedListener, publicationListener);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(
            session,
            updateControl,
            rawUpdater,
            registration,
            binaryDataType,
            jsonDataType);
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

    @SuppressWarnings("unchecked")
    @Test
    public void jsonContext() {
        final EventedUpdateSource source = client.addService(serviceConfig);
        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        updateSourceCaptor.getValue().onActive("a/topic", rawUpdater);

        final UpdateContext<JSON> updateContext = client.createUpdateContext(
            serviceConfig,
            endpointConfig,
            jsonDataType);

        verify(session).feature(TopicUpdateControl.class);

        updateContext.publish(json);

        verify(jsonDataType).toBytes(json);
        verify(session).getState();
        verify(rawUpdater).update(eq("a/topic"), eq(json), eq("a/topic"), isA(UpdateContextCallback.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void binaryContext() {
        final EventedUpdateSource source = client.addService(serviceConfig);
        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        updateSourceCaptor.getValue().onActive("a/topic", rawUpdater);

        final UpdateContext<Binary> updateContext = client.createUpdateContext(
            serviceConfig,
            endpointConfig,
            binaryDataType);

        verify(session).feature(TopicUpdateControl.class);

        updateContext.publish(binary);

        verify(binaryDataType).toBytes(binary);
        verify(session).getState();
        verify(rawUpdater).update(eq("a/topic"), eq(binary), eq("a/topic"), isA(UpdateContextCallback.class));
    }

    @Test(expected = IllegalStateException.class)
    public void noUpdater() {
        client.createUpdateContext(serviceConfig, endpointConfig, jsonDataType);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void contextRecovery() {
        final EventedUpdateSource source = client.addService(serviceConfig);
        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).registerUpdateSource(eq("a"), updateSourceCaptor.capture());

        updateSourceCaptor.getValue().onActive("a/topic", rawUpdater);

        when(session.getState()).thenReturn(RECOVERING_RECONNECT);

        final UpdateContext<JSON> updateContext = client.createUpdateContext(
            serviceConfig,
            endpointConfig,
            jsonDataType);

        verify(session).feature(TopicUpdateControl.class);

        updateContext.publish(json);

        verify(jsonDataType).toBytes(json);
        verify(session).getState();
        verify(rawUpdater, never()).update(eq("a/topic"), eq(json), eq("a/topic"), isA(UpdateContextCallback.class));

        sessionListener.onSessionStateChanged(session, RECOVERING_RECONNECT, CONNECTED_ACTIVE);

        verify(jsonDataType).toBytes(json);
        verify(rawUpdater).update(eq("a/topic"), eq(json), eq("a/topic"), isA(UpdateContextCallback.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void nonExclusiveUpdateContext() {
        final UpdateContext<JSON> updateContext = client.createUpdateContext(
            "a/topic",
            jsonDataType);
        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).updater();

        verify(session).feature(TopicUpdateControl.class);

        updateContext.publish(json);

        verify(jsonDataType).toBytes(json);
        verify(session).getState();
        verify(rawUpdater).update(eq("a/topic"), eq(json), eq("a/topic"), isA(UpdateContextCallback.class));
    }
}
