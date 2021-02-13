/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

import static com.pushtechnology.diffusion.client.Diffusion.updateConstraints;
import static com.pushtechnology.diffusion.client.features.TopicCreationResult.EXISTS;
import static com.pushtechnology.diffusion.client.session.Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.RECOVERING_RECONNECT;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.session.management.EventedSessionListener;
import com.pushtechnology.diffusion.client.features.TopicCreationResult;
import com.pushtechnology.diffusion.client.features.TopicUpdate;
import com.pushtechnology.diffusion.client.features.UpdateStream;
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
public final class PublishingClientImplTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Session session;
    @Mock
    private TopicUpdate topicUpdate;
    @Mock
    private Session.SessionLock sessionLock;
    @Mock
    private CompletableFuture<Session.SessionLock> registration;
    @Mock
    private JSON json;
    @Mock
    private Binary binary;
    @Mock
    private PublicationListener publicationListener;
    @Mock
    private PublicationListener.PublicationCompletionListener completionListener;
    @Mock
    private DataType<Binary> binaryDataType;
    @Mock
    private DataType<JSON> jsonDataType;
    @Mock
    private UpdateStream<JSON> jsonStream;
    @Mock
    private UpdateStream<Binary> binaryStream;
    @Mock
    private CompletableFuture<TopicCreationResult> setFuture;
    @Captor
    private ArgumentCaptor<BiConsumer<Session.SessionLock, Throwable>> registrationHandler;

    private Session.Listener sessionListener;
    private EventedSessionListener eventedListener;
    private EndpointConfig endpointConfig;
    private ServiceConfig serviceConfig;

    private PublishingClient client;

    @Before
    public void setUp() {
        when(session.getState()).thenReturn(CONNECTED_ACTIVE);
        when(session.feature(TopicUpdate.class)).thenReturn(topicUpdate);
        when(binaryDataType.toBytes(binary)).thenReturn(binary);
        when(jsonDataType.toBytes(json)).thenReturn(json);
        when(session.lock(isNotNull(), isNotNull())).thenReturn(registration);
        when(registration.whenComplete(isNotNull())).thenReturn(registration);
        when(jsonDataType.getTypeName()).thenReturn("json");
        when(binaryDataType.getTypeName()).thenReturn("binary");
        when(topicUpdate.createUpdateStream(isNotNull(), eq(JSON.class))).thenReturn(jsonStream);
        when(topicUpdate.createUpdateStream(isNotNull(), eq(JSON.class), isNotNull())).thenReturn(jsonStream);
        when(jsonStream.set(isNotNull())).thenReturn(setFuture);
        when(topicUpdate.createUpdateStream(isNotNull(), eq(Binary.class))).thenReturn(binaryStream);
        when(topicUpdate.createUpdateStream(isNotNull(), eq(Binary.class), isNotNull())).thenReturn(binaryStream);
        when(binaryStream.set(isNotNull())).thenReturn(setFuture);
        when(setFuture.whenComplete(isNotNull())).then(AdditionalAnswers.answer((BiConsumer<TopicCreationResult, Throwable> consumer) -> {
            consumer.accept(EXISTS, null);
            return null;
        }));
        when(publicationListener.onPublicationRequest(isNotNull(), anyInt())).thenReturn(completionListener);
        when(sessionLock.getName()).thenReturn("lock");
        when(sessionLock.getScope()).thenReturn(UNLOCK_ON_CONNECTION_LOSS);
        when(sessionLock.getSequence()).thenReturn(1L);

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
            topicUpdate,
            registration,
            binaryDataType,
            jsonDataType);
    }

    @Test
    public void addService() {
        client.addService(serviceConfig);

        verify(session).lock("a", UNLOCK_ON_CONNECTION_LOSS);
        verify(registration).whenComplete(isNotNull());
    }

    @Test
    public void addServiceStandby() {
        client.addService(serviceConfig);

        verify(session).lock("a", UNLOCK_ON_CONNECTION_LOSS);
        verify(registration).whenComplete(isNotNull());
    }

    @Test
    public void removeService() {
        client.addService(serviceConfig);

        verify(session).lock("a", UNLOCK_ON_CONNECTION_LOSS);

        final CompletableFuture<?> future = client.removeService(serviceConfig);
        assertFalse(future.isDone());

        verify(registration).cancel(false);
        verify(registration).whenComplete(registrationHandler.capture());
        registrationHandler.getValue().accept(null, new CompletionException(new CancellationException()));
        assertTrue(future.isDone());
    }

    @Test
    public void failToRegister() {
        client.addService(serviceConfig);

        verify(session).lock("a", UNLOCK_ON_CONNECTION_LOSS);

        verify(registration).whenComplete(registrationHandler.capture());
        registrationHandler.getValue().accept(sessionLock, new Exception("test exception"));
    }

    @Test
    public void removeUnknownService() {
        final CompletableFuture<?> future = client.removeService(serviceConfig);

        assertTrue(future.isDone());
    }

    @Test
    public void jsonContext() {
        final EventedUpdateSource source = client.addService(serviceConfig);
        verify(session).lock("a", UNLOCK_ON_CONNECTION_LOSS);
        verify(registration).whenComplete(registrationHandler.capture());
        registrationHandler.getValue().accept(sessionLock, null);

        final UpdateContext<JSON> updateContext = client.createUpdateContext(
            serviceConfig,
            endpointConfig,
            JSON.class,
            jsonDataType);

        verify(session).feature(TopicUpdate.class);
        verify(topicUpdate).createUpdateStream("a/topic", JSON.class, updateConstraints().locked(sessionLock));

        updateContext.publish(json);

        verify(jsonDataType).toBytes(json);
        verify(session).getState();
        verify(jsonStream).set(json);
    }

    @Test
    public void binaryContext() {
        final EventedUpdateSource source = client.addService(serviceConfig);
        verify(session).lock("a", UNLOCK_ON_CONNECTION_LOSS);
        verify(registration).whenComplete(registrationHandler.capture());
        registrationHandler.getValue().accept(sessionLock, null);

        final UpdateContext<Binary> updateContext = client.createUpdateContext(
            serviceConfig,
            endpointConfig,
            Binary.class,
            binaryDataType);

        verify(session).feature(TopicUpdate.class);
        verify(topicUpdate).createUpdateStream("a/topic", Binary.class, updateConstraints().locked(sessionLock));

        updateContext.publish(binary);

        verify(binaryDataType).toBytes(binary);
        verify(session).getState();
        verify(binaryStream).set(binary);
    }

    @Test
    public void noUpdater() {
        assertThrows(
            IllegalStateException.class,
            () -> client.createUpdateContext(serviceConfig, endpointConfig, JSON.class, jsonDataType));
    }

    @Test
    public void contextRecovery() {
        final EventedUpdateSource source = client.addService(serviceConfig);
        verify(session).lock("a", UNLOCK_ON_CONNECTION_LOSS);
        verify(registration).whenComplete(registrationHandler.capture());
        registrationHandler.getValue().accept(sessionLock, null);

        when(session.getState()).thenReturn(RECOVERING_RECONNECT);

        final UpdateContext<JSON> updateContext = client.createUpdateContext(
            serviceConfig,
            endpointConfig,
            JSON.class,
            jsonDataType);

        verify(session).feature(TopicUpdate.class);
        verify(topicUpdate).createUpdateStream("a/topic", JSON.class, updateConstraints().locked(sessionLock));

        updateContext.publish(json);

        verify(jsonDataType).toBytes(json);
        verify(session).getState();
        verify(jsonStream, never()).set(json);

        sessionListener.onSessionStateChanged(session, RECOVERING_RECONNECT, CONNECTED_ACTIVE);

        verify(jsonDataType).toBytes(json);
        verify(jsonStream).set(json);
    }
}
