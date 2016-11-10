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

import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.content.Content;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl;
import com.pushtechnology.diffusion.client.session.SessionId;
import com.pushtechnology.diffusion.client.types.ReceiveContext;
import com.pushtechnology.diffusion.content.ContentImpl;

/**
 * Unit tests for {@link RequestManager}.
 *
 * @author Push Technology Limited
 */
public final class RequestManagerTest {

    @Mock
    private MessagingControl messagingControl;

    @Mock
    private SessionId sessionId;

    @Mock
    private ReceiveContext context;

    @Mock
    private RequestManager.RequestHandler requestHandler;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    @Captor
    private ArgumentCaptor<MessagingControl.MessageHandler> handlerCaptor;

    @Captor
    private ArgumentCaptor<RequestManager.Responder> responderCaptor;

    private Content emptyMessage;
    private Content createServiceMessage;


    @Before
    public void setUp() {
        initMocks(this);

        emptyMessage = new ContentImpl(Diffusion
            .dataTypes()
            .json()
            .fromJsonString("{}")
            .toByteArray());
        createServiceMessage = new ContentImpl(Diffusion
            .dataTypes()
            .json()
            .fromJsonString("{\"type\":\"create-service\",\"id\":1,\"service\":{\"name\":\"\",\"host\":\"\",\"port\":80,\"secure\":\"false\",\"pollPeriod\":5000,\"topicPathRoot\":\"\"}}")
            .toByteArray());
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(sessionId, context, messagingControl, requestHandler);
    }

    @Test
    public void wrongPath() {
        final RequestManager requestManager = new RequestManager(messagingControl);
        requestManager.addHandler(ClientControlledModelStore.CONTROL_PATH, requestHandler);

        verify(messagingControl).addMessageHandler(eq(ClientControlledModelStore.CONTROL_PATH), handlerCaptor.capture());

        final MessagingControl.MessageHandler messageHandler = handlerCaptor.getValue();
        messageHandler.onMessage(
            sessionId,
            ClientControlledModelStore.CONTROL_PATH + "/child",
            createServiceMessage,
            context);
    }

    @Test
    public void onEmptyMessage() {
        final RequestManager requestManager = new RequestManager(messagingControl);
        requestManager.addHandler(ClientControlledModelStore.CONTROL_PATH, requestHandler);

        verify(messagingControl).addMessageHandler(eq(ClientControlledModelStore.CONTROL_PATH), handlerCaptor.capture());

        final MessagingControl.MessageHandler messageHandler = handlerCaptor.getValue();

        messageHandler.onMessage(sessionId, ClientControlledModelStore.CONTROL_PATH, new ContentImpl(new byte[0]), context);

    }

    @Test
    public void onEmptyObjectMessage() {
        final RequestManager requestManager = new RequestManager(messagingControl);
        requestManager.addHandler(ClientControlledModelStore.CONTROL_PATH, requestHandler);

        verify(messagingControl).addMessageHandler(eq(ClientControlledModelStore.CONTROL_PATH), handlerCaptor.capture());

        final MessagingControl.MessageHandler messageHandler = handlerCaptor.getValue();

        messageHandler.onMessage(sessionId, ClientControlledModelStore.CONTROL_PATH, emptyMessage, context);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onCreateServiceMessage() {
        final RequestManager requestManager = new RequestManager(messagingControl);
        requestManager.addHandler(ClientControlledModelStore.CONTROL_PATH, requestHandler);

        verify(messagingControl).addMessageHandler(eq(ClientControlledModelStore.CONTROL_PATH), handlerCaptor.capture());

        final MessagingControl.MessageHandler messageHandler = handlerCaptor.getValue();

        messageHandler.onMessage(sessionId, ClientControlledModelStore.CONTROL_PATH, createServiceMessage, context);

        verify(requestHandler).onRequest(isA(Map.class), responderCaptor.capture());

        final RequestManager.Responder value = responderCaptor.getValue();

        value.respond(emptyMap());

        verify(messagingControl).send(eq(sessionId), eq(ClientControlledModelStore.CONTROL_PATH), isA(Content.class), isA(MessagingControl.SendCallback.class));
    }
}
