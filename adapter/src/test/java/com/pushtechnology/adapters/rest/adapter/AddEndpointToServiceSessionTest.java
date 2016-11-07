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

package com.pushtechnology.adapters.rest.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;

/**
 * Unit tests for {@link AddEndpointToServiceSession}.
 *
 * @author Push Technology Limited
 */
public final class AddEndpointToServiceSessionTest {

    @Mock
    private ServiceSession serviceSession;

    private final EndpointConfig endpoint = EndpointConfig
        .builder()
        .name("endpoint")
        .topicPath("path")
        .url("url")
        .produces("json")
        .build();

    private AddEndpointToServiceSession addEndpointToServiceSession;

    @Before
    public void setUp() {
        initMocks(this);

        addEndpointToServiceSession = new AddEndpointToServiceSession(endpoint, serviceSession);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(serviceSession);
    }

    @Test
    public void onSuccess() {
        addEndpointToServiceSession.onTopicAdded("a/topic");

        verify(serviceSession).addEndpoint(endpoint);
    }

    @Test
    public void onFailed() {
        addEndpointToServiceSession.onTopicAddFailed("a/topic", TopicAddFailReason.PERMISSIONS_FAILURE);
    }

    @Test
    public void onDiscard() {
        addEndpointToServiceSession.onDiscard();
    }
}
