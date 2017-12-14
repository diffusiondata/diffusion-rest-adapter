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

package com.pushtechnology.adapters.rest.publication;

import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_BY_CLIENT;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.RECOVERING_RECONNECT;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.diffusion.client.content.update.ContentUpdateFactory;
import com.pushtechnology.diffusion.client.content.update.Update;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater.UpdateContextCallback;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.BinaryDelta;
import com.pushtechnology.diffusion.datatype.Bytes;
import com.pushtechnology.diffusion.datatype.DataType;
import com.pushtechnology.diffusion.datatype.DeltaType;
import com.pushtechnology.diffusion.datatype.binary.Binary;

/**
 * Unit tests for {@link UpdateContextImpl}.
 *
 * @author Push Technology Limited
 */
@SuppressWarnings("deprecation")
public final class UpdateContextImplTest {
    @Mock
    private Session session;
    @Mock
    private Updater updater;
    @Mock
    private Binary binary;
    @Mock
    private ListenerNotifier notifier;
    @Mock
    private DataType<Binary> dataType;
    @Mock
    private DeltaType<Binary, BinaryDelta> deltaType;
    @Mock
    private BinaryDelta delta;
    @Mock
    private Bytes bytes;
    @Mock
    private Update update;
    @Mock
    private TopicUpdateControl updateControl;
    @Mock
    private ContentUpdateFactory updateFactory;

    private UpdateContextImpl<Binary> updateContext;

    @Before
    public void setUp() {
        initMocks(this);

        when(dataType.toBytes(binary)).thenReturn(binary);
        when(dataType.deltaType(BinaryDelta.class)).thenReturn(deltaType);
        when(deltaType.diff(binary, binary)).thenReturn(delta);
        when(deltaType.toBytes(delta)).thenReturn(bytes);
        when(bytes.toByteArray()).thenReturn(new byte[0]);
        when(session.feature(TopicUpdateControl.class)).thenReturn(updateControl);
        when(updateControl.updateFactory(ContentUpdateFactory.class)).thenReturn(updateFactory);
        when(updateFactory.apply(isNotNull())).thenReturn(update);

        updateContext = new UpdateContextImpl<>(session, updater, "a/topic", dataType, notifier);

        verify(dataType).deltaType(BinaryDelta.class);
        verify(session).feature(TopicUpdateControl.class);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(session, updater, notifier, dataType, deltaType);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublish() {
        when(session.getState()).thenReturn(CONNECTED_ACTIVE);
        updateContext.publish(binary);

        verify(session).getState();
        verify(dataType).toBytes(binary);
        verify(updater).update(eq("a/topic"), eq(binary), eq("a/topic"), isA(UpdateContextCallback.class));
        verify(notifier).notifyPublicationRequest(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishDeltas() {
        when(delta.hasChanges()).thenReturn(true);
        when(session.getState()).thenReturn(CONNECTED_ACTIVE);
        updateContext.publish(binary);

        verify(session).getState();
        verify(dataType).toBytes(binary);
        verify(updater).update(eq("a/topic"), eq(binary), eq("a/topic"), isA(UpdateContextCallback.class));
        verify(notifier).notifyPublicationRequest(0);

        updateContext.publish(binary);

        verify(session, times(2)).getState();
        verify(deltaType).diff(binary, binary);
        verify(deltaType).toBytes(delta);
        verify(updater).update(eq("a/topic"), eq(update), eq("a/topic"), isA(UpdateContextCallback.class));
        verify(notifier, times(2)).notifyPublicationRequest(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNoPublishWithoutChange() {
        when(delta.hasChanges()).thenReturn(false);
        when(session.getState()).thenReturn(CONNECTED_ACTIVE);
        updateContext.publish(binary);

        verify(session).getState();
        verify(dataType).toBytes(binary);
        verify(updater).update(eq("a/topic"), eq(binary), eq("a/topic"), isA(UpdateContextCallback.class));
        verify(notifier).notifyPublicationRequest(0);

        updateContext.publish(binary);

        verify(session, times(2)).getState();
        verify(deltaType).diff(binary, binary);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRecovery() {
        when(session.getState()).thenReturn(RECOVERING_RECONNECT);
        updateContext.publish(binary);
        verify(updater, never()).update(eq("a/topic"), eq(binary), eq("a/topic"), isA(UpdateContextCallback.class));
        verify(notifier).notifyPublicationRequest(0);

        updateContext.onSessionStateChanged(session, RECOVERING_RECONNECT, CONNECTED_ACTIVE);

        verify(session).getState();
        verify(dataType).toBytes(binary);
        verify(updater).update(eq("a/topic"), eq(binary), eq("a/topic"), isA(UpdateContextCallback.class));
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
