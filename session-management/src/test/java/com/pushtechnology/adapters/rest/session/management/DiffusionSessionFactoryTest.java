/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.session.management;

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;

/**
 * Unit tests for {@link DiffusionSessionFactory}.
 *
 * @author Push Technology Limited
 */
public final class DiffusionSessionFactoryTest {
    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    private final SessionLostListener lostListener = new SessionLostListener(null);
    private final EventedSessionListener listener = new EventedSessionListener();
    private final DiffusionConfig noAuthModel = DiffusionConfig
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

    private final DiffusionConfig authModel = DiffusionConfig
        .builder()
        .host("localhost")
        .port(8080)
        .principal("control")
        .password("password")
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
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(session, sessionFactory);
    }

    @Test
    public void provideNoAuth() {
        final DiffusionSessionFactory diffusionSessionFactory = new DiffusionSessionFactory(sessionFactory);

        verify(sessionFactory).transports(WEBSOCKET);

        assertEquals(session, diffusionSessionFactory.openSession(noAuthModel, lostListener, listener, null));

        verify(sessionFactory).listener(isA(Session.Listener.class));
        verify(sessionFactory).serverHost("localhost");
        verify(sessionFactory).serverPort(8080);
        verify(sessionFactory).secureTransport(false);
        verify(sessionFactory).connectionTimeout(10000);
        verify(sessionFactory).reconnectionTimeout(10000);
        verify(sessionFactory).maximumMessageSize(32000);
        verify(sessionFactory).inputBufferSize(32000);
        verify(sessionFactory).outputBufferSize(32000);
        verify(sessionFactory).recoveryBufferSize(256);
        verify(sessionFactory).open();
    }

    @Test
    public void provideAuth() {
        final DiffusionSessionFactory diffusionSessionFactory = new DiffusionSessionFactory(sessionFactory);

        verify(sessionFactory).transports(WEBSOCKET);

        assertEquals(session, diffusionSessionFactory.openSession(authModel, lostListener, listener, null));

        verify(sessionFactory).listener(isA(Session.Listener.class));
        verify(sessionFactory).serverHost("localhost");
        verify(sessionFactory).serverPort(8080);
        verify(sessionFactory).secureTransport(false);
        verify(sessionFactory).principal("control");
        verify(sessionFactory).password("password");
        verify(sessionFactory).connectionTimeout(10000);
        verify(sessionFactory).reconnectionTimeout(10000);
        verify(sessionFactory).maximumMessageSize(32000);
        verify(sessionFactory).inputBufferSize(32000);
        verify(sessionFactory).outputBufferSize(32000);
        verify(sessionFactory).recoveryBufferSize(256);
        verify(sessionFactory).open();
    }
}
