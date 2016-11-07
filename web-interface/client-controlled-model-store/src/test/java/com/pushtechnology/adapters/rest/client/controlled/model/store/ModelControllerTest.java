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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.store.AsyncMutableModelStore;
import com.pushtechnology.diffusion.client.session.SessionId;
import com.pushtechnology.diffusion.client.types.ReceiveContext;

/**
 * Unit tests for {@link ModelController}.
 *
 * @author Push Technology Limited
 */
public final class ModelControllerTest {

    @Mock
    private ScheduledExecutorService executor;

    @Mock
    private SessionId sessionId;

    @Mock
    private ReceiveContext context;

    @Mock
    private RequestManager.Responder responder;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private AsyncMutableModelStore modelStore;

    @Before
    public void setUp() {
        initMocks(this);

        modelStore = new AsyncMutableModelStore(executor);

        // Set the initial model
        modelStore.setModel(Model
            .builder()
            .diffusion(DiffusionConfig
                .builder()
                .host("localhost")
                .principal("control")
                .password("password")
                .build())
            .services(emptyList())
            .build());
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(executor, sessionId, context, responder);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onEmptyMessage() {
        final ModelController controller = new ModelController(modelStore);

        controller.onRequest(emptyMap(), responder);
        verify(responder).respond(isA(Map.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onUnknownMessage() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> unknownTypeMessage = new HashMap<>();
        unknownTypeMessage.put("type", "ha, ha");

        controller.onRequest(unknownTypeMessage, responder);
        verify(responder).respond(isA(Map.class));
    }

    @Test
    public void onCreateServiceMessage() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message = new HashMap<>();
        message.put("type", "create-service");
        final Map<String, Object> service = new HashMap<>();
        service.put("name", "");
        service.put("host", "");
        service.put("port", 80);
        service.put("secure", false);
        service.put("pollPeriod", 50000);
        service.put("topicRoot", "");

        message.put("service", service);

        controller.onRequest(message, responder);

        verify(executor, times(2)).execute(runnableCaptor.capture());
        verify(responder).respond(emptyMap());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onCreateServiceMessageWithoutService() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message = new HashMap<>();
        message.put("type", "create-service");

        controller.onRequest(message, responder);
        verify(responder).respond(isA(Map.class));
    }
}
