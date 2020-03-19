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

package com.pushtechnology.adapters.rest.client.controlled.model.store;

import static org.mockito.ArgumentMatchers.eq;
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
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link ClientControlledModelStore}.
 *
 * @author Push Technology Limited
 */
public final class ClientControlledModelStoreTest {

    @Mock
    private Session session;

    @Mock
    private MessagingControl messagingControl;

    @Mock
    private TopicControl topicControl;

    @Mock
    private ScheduledExecutorService executor;

    @Captor
    private ArgumentCaptor<MessagingControl.RequestHandler<JSON, JSON>> requestHandlerCaptor;

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

        when(session.feature(MessagingControl.class)).thenReturn(messagingControl);
        when(session.feature(TopicControl.class)).thenReturn(topicControl);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(session, messagingControl, executor);
    }

    @Test
    public void sessionReady() {
        final ClientControlledModelStore modelStore = new ClientControlledModelStore(
            executor,
            diffusionConfig);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        modelStore.onSessionReady(session);

        verify(session).feature(MessagingControl.class);
        verify(messagingControl).addRequestHandler(
            eq("adapter/rest/model/store"),
            eq(JSON.class),
            eq(JSON.class),
            requestHandlerCaptor.capture());
    }
}
