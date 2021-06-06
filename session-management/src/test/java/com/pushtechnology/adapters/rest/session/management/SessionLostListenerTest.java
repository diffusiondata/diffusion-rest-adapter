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

package com.pushtechnology.adapters.rest.session.management;

import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_BY_CLIENT;
import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_BY_SERVER;
import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_FAILED;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTING;
import static com.pushtechnology.diffusion.client.session.Session.State.RECOVERING_RECONNECT;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.pushtechnology.diffusion.client.session.Session;

/**
 * Unit tests for {@link SessionLostListener}.
 *
 * @author Push Technology Limited
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness= Strictness.LENIENT)
public final class SessionLostListenerTest {
    @Mock
    private Session session;
    @Mock
    private SessionLossHandler sessionLossHandler;

    private SessionLostListener sessionLostListener;

    @BeforeEach
    public void setUp() {
        sessionLostListener = new SessionLostListener(sessionLossHandler);
    }

    @AfterEach
    public void postConditions() {
        verifyNoMoreInteractions(session, sessionLossHandler);
    }

    @Test
    public void onStart() {
        sessionLostListener.onSessionStateChanged(session, CONNECTING, CONNECTED_ACTIVE);
    }

    @Test
    public void onEnteringRecovery() {
        sessionLostListener.onSessionStateChanged(session, CONNECTED_ACTIVE, RECOVERING_RECONNECT);
    }

    @Test
    public void onClose() {
        sessionLostListener.onSessionStateChanged(session, CONNECTED_ACTIVE, CLOSED_BY_CLIENT);
    }

    @Test
    public void onFailure() {
        sessionLostListener.onSessionStateChanged(session, CONNECTED_ACTIVE, CLOSED_FAILED);

        verify(sessionLossHandler).onLoss();
    }

    @Test
    public void onKickedOff() {
        sessionLostListener.onSessionStateChanged(session, CONNECTED_ACTIVE, CLOSED_BY_SERVER);

        verify(sessionLossHandler).onLoss();
    }
}
