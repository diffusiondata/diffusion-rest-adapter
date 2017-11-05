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

package com.pushtechnology.adapters.rest.client.controlled.model.store;

import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
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
import com.pushtechnology.diffusion.client.features.Messaging;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl.RequestHandler.RequestContext;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl.RequestHandler.Responder;
import com.pushtechnology.diffusion.client.session.SessionId;
import com.pushtechnology.diffusion.datatype.json.JSON;

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
    private RequestContext context;

    @Mock
    private RequestManager.RequestHandler requestHandler;

    @Mock
    private Responder<JSON> responder;

    @Captor
    private ArgumentCaptor<MessagingControl.RequestHandler<JSON, JSON>> handlerCaptor;

    @Captor
    private ArgumentCaptor<Responder<Map<String, Object>>> responderCaptor;

    private JSON emptyMessage;
    private JSON createServiceMessage;

    @Before
    public void setUp() {
        initMocks(this);

        emptyMessage = Diffusion
            .dataTypes()
            .json()
            .fromJsonString("{}");
        createServiceMessage = Diffusion
            .dataTypes()
            .json()
            .fromJsonString("{\"type\":\"create-service\",\"id\":1,\"service\":{\"name\":\"\",\"host\":\"\",\"port\":80,\"secure\":\"false\",\"pollPeriod\":5000,\"topicPathRoot\":\"\"}}");

        when(context.getPath()).thenReturn(ClientControlledModelStore.CONTROL_PATH);
        when(context.getSessionId()).thenReturn(sessionId);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(sessionId, context, messagingControl, requestHandler);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void wrongPath() {
        final RequestManager requestManager = new RequestManager(messagingControl);
        requestManager.addHandler(ClientControlledModelStore.CONTROL_PATH, requestHandler);

        verify(messagingControl).addRequestHandler(eq(ClientControlledModelStore.CONTROL_PATH), eq(JSON.class), eq(JSON.class), handlerCaptor.capture());

        final MessagingControl.RequestHandler<JSON, JSON> messageHandler = handlerCaptor.getValue();
        messageHandler.onRequest(
            createServiceMessage,
            context,
            responder);

        verify(context).getPath();
        verify(requestHandler).onRequest(isA(Map.class), isA(Responder.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onEmptyMessage() {
        final RequestManager requestManager = new RequestManager(messagingControl);
        requestManager.addHandler(ClientControlledModelStore.CONTROL_PATH, requestHandler);

        verify(messagingControl).addRequestHandler(eq(ClientControlledModelStore.CONTROL_PATH), eq(JSON.class), eq(JSON.class), handlerCaptor.capture());

        final MessagingControl.RequestHandler<JSON, JSON> messageHandler = handlerCaptor.getValue();

        messageHandler.onRequest(
            emptyMessage,
            context,
            responder);


        verify(requestHandler).onRequest(isA(Map.class), isA(Responder.class));
        verify(context).getPath();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onEmptyObjectMessage() {
        final RequestManager requestManager = new RequestManager(messagingControl);
        requestManager.addHandler(ClientControlledModelStore.CONTROL_PATH, requestHandler);

        verify(messagingControl).addRequestHandler(eq(ClientControlledModelStore.CONTROL_PATH), eq(JSON.class), eq(JSON.class), handlerCaptor.capture());

        final MessagingControl.RequestHandler<JSON, JSON> messageHandler = handlerCaptor.getValue();

        messageHandler.onRequest(
            emptyMessage,
            context,
            responder);

        verify(requestHandler).onRequest(isA(Map.class), isA(Responder.class));
        verify(context).getPath();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onCreateServiceMessage() {
        final RequestManager requestManager = new RequestManager(messagingControl);
        requestManager.addHandler(ClientControlledModelStore.CONTROL_PATH, requestHandler);

        verify(messagingControl).addRequestHandler(eq(ClientControlledModelStore.CONTROL_PATH), eq(JSON.class), eq(JSON.class), handlerCaptor.capture());

        final MessagingControl.RequestHandler<JSON, JSON> messageHandler = handlerCaptor.getValue();

        messageHandler.onRequest(
            createServiceMessage,
            context,
            responder);

        verify(requestHandler).onRequest(isA(Map.class), isA(Responder.class));
        verify(context).getPath();
        verify(requestHandler).onRequest(isA(Map.class), responderCaptor.capture());

        final Responder<Map<String, Object>> value = responderCaptor.getValue();

        value.respond(emptyMap());

        verify(responder).respond(isA(JSON.class));
    }
}
