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

package com.pushtechnology.adapters.rest.client.controlled.model.store;

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.ScheduledExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.session.management.DiffusionSessionFactory;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;

/**
 * Unit tests for {@link ClientControlledModelStore}.
 *
 * @author Push Technology Limited
 */
public final class ClientControlledModelStoreTest {
    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private MessagingControl messagingControl;

    @Mock
    private ScheduledExecutorService executor;

    @Captor
    private ArgumentCaptor<MessagingControl.MessageHandler> messageHandlerCaptor;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private final DiffusionConfig diffusionConfig = DiffusionConfig
        .builder()
        .host("localhost")
        .port(8080)
        .connectionTimeout(10000)
        .reconnectionTimeout(10000)
        .maximumMessageSize(32000)
        .inputBufferSize(32000)
        .outputBufferSize(32000)
        .recoveryBufferSize(256)
        .build();

    @Before
    public void setUp() {
        initMocks(this);

        when(sessionFactory.transports(WEBSOCKET)).thenReturn(sessionFactory);
        when(sessionFactory.listener(isA(Session.Listener.class))).thenReturn(sessionFactory);
        when(sessionFactory.serverHost("localhost")).thenReturn(sessionFactory);
        when(sessionFactory.serverPort(8080)).thenReturn(sessionFactory);
        when(sessionFactory.secureTransport(false)).thenReturn(sessionFactory);
        when(sessionFactory.principal("control")).thenReturn(sessionFactory);
        when(sessionFactory.password("password")).thenReturn(sessionFactory);
        when(sessionFactory.connectionTimeout(10000)).thenReturn(sessionFactory);
        when(sessionFactory.reconnectionTimeout(10000)).thenReturn(sessionFactory);
        when(sessionFactory.maximumMessageSize(32000)).thenReturn(sessionFactory);
        when(sessionFactory.inputBufferSize(32000)).thenReturn(sessionFactory);
        when(sessionFactory.outputBufferSize(32000)).thenReturn(sessionFactory);
        when(sessionFactory.recoveryBufferSize(256)).thenReturn(sessionFactory);
        when(sessionFactory.open()).thenReturn(session);

        when(session.feature(MessagingControl.class)).thenReturn(messagingControl);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(session, messagingControl, executor);
    }

    @Test
    public void startClose() {
        final ClientControlledModelStore modelStore = new ClientControlledModelStore(
            executor,
            diffusionConfig,
            null,
            new DiffusionSessionFactory(sessionFactory));

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        modelStore.start();

        verify(sessionFactory).open();
        verify(session).feature(MessagingControl.class);
        verify(messagingControl).addMessageHandler(eq("adapter/rest/model/store"), messageHandlerCaptor.capture());

        modelStore.close();

        verify(session).close();
    }
}
