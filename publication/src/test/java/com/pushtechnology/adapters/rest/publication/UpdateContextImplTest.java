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

import static com.pushtechnology.adapters.rest.publication.UpdateTopicCallback.INSTANCE;
import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_BY_CLIENT;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.RECOVERING_RECONNECT;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.ValueUpdater;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.binary.Binary;

/**
 * Unit tests for {@link UpdateContextImpl}.
 *
 * @author Push Technology Limited
 */
public final class UpdateContextImplTest {
    @Mock
    private Session session;
    @Mock
    private ValueUpdater<Binary> updater;
    @Mock
    private Binary binary;

    private UpdateContextImpl<Binary> updateContext;

    @Before
    public void setUp() {
        initMocks(this);

        updateContext = new UpdateContextImpl<>(session, updater, "a/topic");
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(session, updater);
    }

    @Test
    public void testPublish() {
        when(session.getState()).thenReturn(CONNECTED_ACTIVE);
        updateContext.publish(binary);

        verify(session).getState();
        verify(updater).update("a/topic", binary, "a/topic", INSTANCE);
    }

    @Test
    public void testRecovery() {
        when(session.getState()).thenReturn(RECOVERING_RECONNECT);
        updateContext.publish(binary);
        verify(updater, never()).update("a/topic", binary, "a/topic", INSTANCE);

        updateContext.onSessionStateChanged(session, RECOVERING_RECONNECT, CONNECTED_ACTIVE);

        verify(session).getState();
        verify(updater).update("a/topic", binary, "a/topic", INSTANCE);
    }

    @Test(expected = IllegalStateException.class)
    public void testClosed() {
        when(session.getState()).thenReturn(CLOSED_BY_CLIENT);

        try {
            updateContext.publish(binary);
        }
        finally {
            verify(session).getState();
        }
    }
}