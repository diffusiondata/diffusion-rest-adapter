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

package com.pushtechnology.adapters.rest.session.management;

import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTING;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;

/**
 * Unit tests for {@link EventedSessionListener}.
 *
 * @author Push Technology Limited
 */
public final class EventedSessionListenerTest {
    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session.Listener listener;
    @Mock
    private Session session;
    @Captor
    private ArgumentCaptor<Session.Listener> listenerCaptor;

    private EventedSessionListener sessionListener;

    @Before
    public void setUp() {
        initMocks(this);

        sessionListener = new EventedSessionListener();
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(sessionFactory, listener, session);
    }

    @Test
    public void notifiesListenersAddedBeforeAddedToSessionFactory() {
        sessionListener
            .onSessionStateChange(listener)
            .addTo(sessionFactory);
        verify(sessionFactory).listener(listenerCaptor.capture());

        listenerCaptor.getValue().onSessionStateChanged(session, CONNECTING, CONNECTED_ACTIVE);
        verify(listener).onSessionStateChanged(session, CONNECTING, CONNECTED_ACTIVE);
    }

    @Test
    public void notifiesListenersAddedAfterAddedToSessionFactory() {
        sessionListener.addTo(sessionFactory);
        verify(sessionFactory).listener(listenerCaptor.capture());

        sessionListener.onSessionStateChange(listener);

        listenerCaptor.getValue().onSessionStateChanged(session, CONNECTING, CONNECTED_ACTIVE);
        verify(listener).onSessionStateChanged(session, CONNECTING, CONNECTED_ACTIVE);
    }
}
