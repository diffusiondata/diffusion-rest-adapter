/*******************************************************************************
 * Copyright (C) 2020 Push Technology Ltd.
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

import static com.pushtechnology.diffusion.client.features.TopicCreationResult.EXISTS;
import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_BY_CLIENT;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.RECOVERING_RECONNECT;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener.PublicationCompletionListener;
import com.pushtechnology.diffusion.client.features.TopicCreationResult;
import com.pushtechnology.diffusion.client.features.TopicUpdate;
import com.pushtechnology.diffusion.client.features.UpdateStream;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.Bytes;
import com.pushtechnology.diffusion.datatype.DataType;
import com.pushtechnology.diffusion.datatype.binary.Binary;

/**
 * Unit tests for {@link ValueUpdateContext}.
 *
 * @author Matt Champion 21/12/2017
 */
public final class ValueUpdateContextTest {
    @Mock
    private Session session;
    @Mock
    private Binary binary;
    @Mock
    private PublicationListener publicationListener;
    @Mock
    private PublicationCompletionListener completionListener;
    @Mock
    private DataType<Binary> dataType;
    @Mock
    private Bytes bytes;
    @Mock
    private TopicUpdate topicUpdate;
    @Mock
    private UpdateStream<Binary> updateStream;
    @Mock
    private CompletableFuture<TopicCreationResult> setFuture;

    private ValueUpdateContext<Binary> updateContext;

    @Before
    public void setUp() {
        initMocks(this);

        when(dataType.toBytes(binary)).thenReturn(binary);
        when(bytes.toByteArray()).thenReturn(new byte[0]);
        when(session.feature(TopicUpdate.class)).thenReturn(topicUpdate);
        when(topicUpdate.createUpdateStream(notNull(), eq(Binary.class))).thenReturn(updateStream);
        when(updateStream.set(notNull())).thenReturn(setFuture);
        when(setFuture.whenComplete(notNull())).then(AdditionalAnswers.answer((BiConsumer<TopicCreationResult, Throwable> consumer) -> {
            consumer.accept(EXISTS, null);
            return null;
        }));
        when(publicationListener.onPublicationRequest(isNotNull(), anyInt())).thenReturn(completionListener);

        updateContext = new ValueUpdateContext<>(
            session,
            "a/topic",
            Binary.class,
            dataType,
            publicationListener);

        verify(session).feature(TopicUpdate.class);
        verify(topicUpdate).createUpdateStream("a/topic", Binary.class);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(session, topicUpdate, publicationListener, completionListener, dataType, updateStream);
    }

    @Test
    public void testPublish() {
        when(session.getState()).thenReturn(CONNECTED_ACTIVE);
        updateContext.publish(binary);

        verify(session).getState();
        verify(dataType).toBytes(binary);
        verify(updateStream).set(binary);
        verify(publicationListener).onPublicationRequest("a/topic", 0);
        verify(completionListener).onPublication();
    }

    @Test
    public void testRecovery() {
        when(session.getState()).thenReturn(RECOVERING_RECONNECT);
        updateContext.publish(binary);
        verify(updateStream, never()).set(binary);
        verify(publicationListener).onPublicationRequest("a/topic", 0);

        updateContext.onSessionStateChanged(session, RECOVERING_RECONNECT, CONNECTED_ACTIVE);

        verify(session).getState();
        verify(dataType).toBytes(binary);
        verify(updateStream).set(binary);
        verify(completionListener).onPublication();
    }

    @Test
    public void testClosed() {
        when(session.getState()).thenReturn(CLOSED_BY_CLIENT);

        assertThrows(IllegalStateException.class, () -> updateContext.publish(binary));
        verify(session).getState();
    }
}
