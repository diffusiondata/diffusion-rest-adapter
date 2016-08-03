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
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;

/**
 * Unit tests for {@link InitialEndpointResponseHandler}.
 *
 * @author Push Technology Limited
 */
public final class InitialEndpointResponseHandlerTest {

    @Mock
    private TopicManagementClient topicManagementClient;
    @Mock
    private EndpointResponse response;
    @Mock
    private TopicControl.AddCallback callback;

    private ServiceConfig serviceConfig = ServiceConfig.builder().build();
    private final EndpointConfig endpointConfig = EndpointConfig.builder().build();

    private InitialEndpointResponseHandler handler;

    @Before
    public void setUp() {
        initMocks(this);

        handler = new InitialEndpointResponseHandler(
            topicManagementClient,
            serviceConfig,
            endpointConfig,
            callback);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(topicManagementClient, response, callback);
    }

    @Test
    public void completed() {
        handler.completed(response);

        verify(topicManagementClient).addEndpoint(serviceConfig, endpointConfig, callback);
    }

    @Test
    public void failed() {
        final Exception ex = new Exception("Intentional for test");
        handler.failed(ex);
    }

    @Test
    public void cancelled() {
        handler.cancelled();
    }
}
