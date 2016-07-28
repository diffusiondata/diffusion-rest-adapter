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

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.callbacks.Registration;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.UpdateSource;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater;

/**
 * Unit tests for {@link EventedUpdateSourceImpl}.
 *
 * @author Push Technology Limited
 */
public final class EventedUpdateSourceTest {
    @Mock
    private TopicUpdateControl updateControl;
    @Mock
    private Updater updater;
    @Mock
    private Registration registration;
    @Mock
    private Runnable standbyEventHandler;
    @Mock
    private Runnable closeEventHandler;
    @Mock
    private Consumer<Updater> activeEventHandler;
    @Mock
    private Consumer<ErrorReason> errorEventHandler;
    @Captor
    private ArgumentCaptor<UpdateSource> updateSourceCaptor;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(
            updateControl,
            updater,
            registration,
            standbyEventHandler,
            closeEventHandler,
            activeEventHandler,
            errorEventHandler);
    }

    @Test
    public void registerCompleteAndClose() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("a/cromulent/topic")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(updateControl);

        verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), updateSourceCaptor.capture());
        updateSourceCaptor.getValue().onRegistered("a/cromulent/topic", registration);

        source.close();
        verify(registration).close();
    }

    @Test
    public void registerPendingCloseAndComplete() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("a/cromulent/topic")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(updateControl);

        verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), updateSourceCaptor.capture());

        source.close();
        verify(registration, never()).close();

        updateSourceCaptor.getValue().onRegistered("a/cromulent/topic", registration);

        verify(registration).close();
    }

    @Test
    public void onActive() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("a/cromulent/topic")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(updateControl);

        verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), updateSourceCaptor.capture());
        updateSourceCaptor.getValue().onRegistered("a/cromulent/topic", registration);

        updateSourceCaptor.getValue().onActive("a/cromulent/topic", updater);
        verify(activeEventHandler).accept(updater);
    }

    @Test
    public void onActiveSetHandler() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("a/cromulent/topic")
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(updateControl);

        verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), updateSourceCaptor.capture());
        updateSourceCaptor.getValue().onRegistered("a/cromulent/topic", registration);

        updateSourceCaptor.getValue().onActive("a/cromulent/topic", updater);
        verify(activeEventHandler, never()).accept(updater);

        source.onActive(activeEventHandler);
        verify(activeEventHandler).accept(updater);
    }

    @Test
    public void onStandby() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("a/cromulent/topic")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(updateControl);

        verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), updateSourceCaptor.capture());
        updateSourceCaptor.getValue().onRegistered("a/cromulent/topic", registration);

        updateSourceCaptor.getValue().onStandby("a/cromulent/topic");
        verify(standbyEventHandler).run();
    }

    @Test
    public void onStandbySetHandler() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("a/cromulent/topic")
            .onActive(activeEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(updateControl);

        verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), updateSourceCaptor.capture());
        updateSourceCaptor.getValue().onRegistered("a/cromulent/topic", registration);

        updateSourceCaptor.getValue().onStandby("a/cromulent/topic");
        verify(standbyEventHandler, never()).run();

        source.onStandby(standbyEventHandler);
        verify(standbyEventHandler).run();
    }

    @Test
    public void onClose() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("a/cromulent/topic")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(updateControl);

        verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), updateSourceCaptor.capture());
        updateSourceCaptor.getValue().onRegistered("a/cromulent/topic", registration);

        updateSourceCaptor.getValue().onStandby("a/cromulent/topic");
        verify(standbyEventHandler).run();

        source.close();
        verify(registration).close();
        verify(closeEventHandler, never()).run();

        updateSourceCaptor.getValue().onClose("a/cromulent/topic");
        verify(closeEventHandler).run();
    }

    @Test
    public void onCloseSetHandler() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("a/cromulent/topic")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onError(errorEventHandler)
            .register(updateControl);

        verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), updateSourceCaptor.capture());
        updateSourceCaptor.getValue().onRegistered("a/cromulent/topic", registration);

        updateSourceCaptor.getValue().onStandby("a/cromulent/topic");
        verify(standbyEventHandler).run();

        source.close();
        verify(registration).close();

        updateSourceCaptor.getValue().onClose("a/cromulent/topic");
        verify(closeEventHandler, never()).run();

        source.onClose(closeEventHandler);
        verify(closeEventHandler).run();
    }

    @Test
    public void onError() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("a/cromulent/topic")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(updateControl);

        verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), updateSourceCaptor.capture());
        updateSourceCaptor.getValue().onRegistered("a/cromulent/topic", registration);

        updateSourceCaptor.getValue().onError("a/cromulent/topic", ErrorReason.COMMUNICATION_FAILURE);

        verify(errorEventHandler).accept(ErrorReason.COMMUNICATION_FAILURE);
    }

    @Test
    public void onErrorSetHandler() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("a/cromulent/topic")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .register(updateControl);

        verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), updateSourceCaptor.capture());
        updateSourceCaptor.getValue().onRegistered("a/cromulent/topic", registration);

        updateSourceCaptor.getValue().onError("a/cromulent/topic", ErrorReason.COMMUNICATION_FAILURE);

        verify(errorEventHandler, never()).accept(ErrorReason.COMMUNICATION_FAILURE);

        source.onError(errorEventHandler);

        verify(errorEventHandler).accept(ErrorReason.COMMUNICATION_FAILURE);
    }

    @Test(expected = IllegalStateException.class)
    public void registerTwice() {
        try {
            new EventedUpdateSourceImpl("a/cromulent/topic")
                .onActive(activeEventHandler)
                .onStandby(standbyEventHandler)
                .onClose(closeEventHandler)
                .onError(errorEventHandler)
                .register(updateControl)
                .register(updateControl);
        }
        finally {
            verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), isA(UpdateSource.class));
        }
    }

    @Test
    public void closeBeforeRegister() {
        new EventedUpdateSourceImpl("a/cromulent/topic")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .close();
    }

    @Test
    public void closeTwice() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("a/cromulent/topic")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(updateControl);
        source.close();
        source.close();
        verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), isA(UpdateSource.class));
    }

    @Test
    public void closeWhileRegistering() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("a/cromulent/topic")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(updateControl);

        verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), updateSourceCaptor.capture());
        updateSourceCaptor.getValue().onRegistered("a/cromulent/topic", registration);

        source.close();
        verify(registration).close();
        updateSourceCaptor.getValue().onStandby("a/cromulent/topic");
        updateSourceCaptor.getValue().onClose("a/cromulent/topic");

        verify(closeEventHandler).run();
    }

    @Test
    public void standbyToActive() {
        final EventedUpdateSource source = new EventedUpdateSourceImpl("a/cromulent/topic")
            .onActive(activeEventHandler)
            .onStandby(standbyEventHandler)
            .onClose(closeEventHandler)
            .onError(errorEventHandler)
            .register(updateControl);

        verify(updateControl).registerUpdateSource(eq("a/cromulent/topic"), updateSourceCaptor.capture());
        updateSourceCaptor.getValue().onRegistered("a/cromulent/topic", registration);

        updateSourceCaptor.getValue().onStandby("a/cromulent/topic");
        verify(standbyEventHandler).run();

        updateSourceCaptor.getValue().onActive("a/cromulent/topic", updater);
        verify(activeEventHandler).accept(updater);
    }
}
