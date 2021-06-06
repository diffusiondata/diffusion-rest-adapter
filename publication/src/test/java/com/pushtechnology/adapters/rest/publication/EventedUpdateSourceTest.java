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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.Session.SessionLock;
import com.pushtechnology.diffusion.client.session.SessionClosedException;

/**
 * Unit tests for {@link EventedUpdateSourceImpl}.
 *
 * @author Push Technology Limited
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness=Strictness.LENIENT)
public final class EventedUpdateSourceTest {
    @Mock
    private Session session;
    @Mock
    private SessionLock sessionLock;
    @Mock
    private CompletableFuture<SessionLock> registration;
    @Mock
    private Runnable standbyEventHandler;
    @Mock
    private Runnable closeEventHandler;
    @Mock
    private Consumer<SessionLock> activeEventHandler;
    @Mock
    private Consumer<ErrorReason> errorEventHandler;
    @Captor
    private ArgumentCaptor<BiConsumer<SessionLock, Throwable>> registrationHandler;

    @BeforeEach
    public void setUp() {
        when(session.lock(isNotNull(), isNotNull())).thenReturn(registration);
        when(registration.whenComplete(isNotNull())).thenReturn(registration);
    }

    @AfterEach
    public void postConditions() {
        verifyNoMoreInteractions(
            session,
            sessionLock,
            registration,
            standbyEventHandler,
            closeEventHandler,
            activeEventHandler,
            errorEventHandler);
    }

    @Test
    public void registerCompleteAndClose() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(session);

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(standbyEventHandler).run();
        verify(registration).whenComplete(registrationHandler.capture());
        registrationHandler.getValue().accept(sessionLock, null);
        verify(activeEventHandler).accept(sessionLock);

        source.close();
        verify(sessionLock).unlock();
        verify(closeEventHandler).run();
    }

    @Test
    public void registerPendingCloseAndComplete() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(session);

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(standbyEventHandler).run();
        verify(registration).whenComplete(isNotNull());

        source.close();

        verify(registration).cancel(false);
    }

    @Test
    public void onActive() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(session);

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(registration).whenComplete(registrationHandler.capture());
        verify(standbyEventHandler).run();

        registrationHandler.getValue().accept(sessionLock, null);

        verify(activeEventHandler).accept(sessionLock);
    }

    @Test
    public void onActiveSetHandler() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(session);

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(standbyEventHandler).run();
        verify(registration).whenComplete(registrationHandler.capture());
        registrationHandler.getValue().accept(sessionLock, null);

        verify(activeEventHandler, never()).accept(sessionLock);

        source.onActive(activeEventHandler);
        verify(activeEventHandler).accept(sessionLock);
    }

    @Test
    public void onStandby() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(session);

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(standbyEventHandler).run();
        verify(registration).whenComplete(isNotNull());
    }

    @Test
    public void onStandbySetHandler() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(session);

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(registration).whenComplete(isNotNull());

        verify(standbyEventHandler, never()).run();

        source.onStandby(standbyEventHandler);
        verify(standbyEventHandler).run();
    }

    @Test
    public void onClose() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(session);

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(standbyEventHandler).run();
        verify(registration).whenComplete(registrationHandler.capture());
        registrationHandler.getValue().accept(sessionLock, null);
        verify(activeEventHandler).accept(sessionLock);

        source.close();
        verify(sessionLock).unlock();
        verify(closeEventHandler).run();
    }

    @Test
    public void onCloseSetHandler() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onError(errorEventHandler)
            .register(session);

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(standbyEventHandler).run();
        verify(registration).whenComplete(registrationHandler.capture());
        registrationHandler.getValue().accept(sessionLock, null);
        verify(activeEventHandler).accept(sessionLock);

        source.close();
        verify(sessionLock).unlock();

        verify(closeEventHandler, never()).run();

        source.onClose(closeEventHandler);
        verify(closeEventHandler).run();
    }

    @Test
    public void onError() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(session);

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(standbyEventHandler).run();
        verify(registration).whenComplete(registrationHandler.capture());
        registrationHandler.getValue().accept(sessionLock, new Exception("test exception"));

        verify(errorEventHandler).accept(ErrorReason.COMMUNICATION_FAILURE);
    }

    @Test
    public void onSessionClosedError() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(session);

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(standbyEventHandler).run();
        verify(registration).whenComplete(registrationHandler.capture());
        registrationHandler.getValue().accept(sessionLock, new CompletionException(new SessionClosedException()));

        verify(closeEventHandler).run();
    }

    @Test
    public void onErrorSetHandler() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .register(session);

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(standbyEventHandler).run();
        verify(registration).whenComplete(registrationHandler.capture());
        registrationHandler.getValue().accept(sessionLock, new Exception("test exception"));

        verify(errorEventHandler, never()).accept(ErrorReason.COMMUNICATION_FAILURE);

        source.onError(errorEventHandler);

        verify(errorEventHandler).accept(ErrorReason.COMMUNICATION_FAILURE);
    }

    @Test
    public void registerTwice() {
        assertThrows(
            IllegalStateException.class,
            () -> new EventedUpdateSourceImpl("serviceName")
                .onActive(activeEventHandler)
                .onStandby(standbyEventHandler)
                .onClose(closeEventHandler)
                .onError(errorEventHandler)
                .register(session)
                .register(session));

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(registration).whenComplete(isNotNull());
        verify(standbyEventHandler).run();
    }

    @Test
    public void closeBeforeRegister() {
        new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .close();
    }

    @Test
    public void closeTwice() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(session);
        source.close();
        source.close();
        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(registration).whenComplete(isNotNull());
        verify(standbyEventHandler).run();
        verify(registration, times(2)).cancel(false);
    }

    @Test
    public void closeWhileRegistering() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(session);

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(standbyEventHandler).run();
        verify(registration).whenComplete(registrationHandler.capture());

        source.close();

        verify(registration).cancel(false);
        registrationHandler.getValue().accept(sessionLock, null);

        verify(closeEventHandler).run();
    }

    @Test
    public void standbyToActive() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("serviceName")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(session);

        verify(session).lock("serviceName", Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS);
        verify(registration).whenComplete(registrationHandler.capture());
        registrationHandler.getValue().accept(sessionLock, null);
        verify(standbyEventHandler).run();
        verify(activeEventHandler).accept(sessionLock);
    }
}
