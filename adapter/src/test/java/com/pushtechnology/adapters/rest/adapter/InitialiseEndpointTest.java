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

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.concurrent.FutureCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointClient;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.publication.UpdateContext;
import com.pushtechnology.adapters.rest.services.ServiceSession;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.AddCallback;

/**
 * Unit tests for {@link InitialiseEndpoint}.
 *
 * @author Push Technology Limited
 */
public final class InitialiseEndpointTest {
    @Mock
    private EndpointClient endpointClient;
    @Mock
    private TopicManagementClient topicManagementClient;
    @Mock
    private PublishingClient publishingClient;
    @Mock
    private UpdateContext updateContext;
    @Mock
    private ServiceSession serviceSession;
    @Mock
    private EndpointResponse response;
    @Captor
    private ArgumentCaptor<FutureCallback<EndpointResponse>> callbackCaptor;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint")
        .url("path")
        .topicPath("topic")
        .produces("json")
        .build();
    private final EndpointConfig inferEndpointConfig = EndpointConfig
        .builder()
        .name("endpoint")
        .url("path")
        .topicPath("topic")
        .produces("auto")
        .build();
    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service")
        .host("example.com")
        .topicPathRoot("path")
        .endpoints(emptyList())
        .build();

    private InitialiseEndpoint initialiseEndpoint;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws IOException {
        initMocks(this);

        when(response.getHeader("content-type")).thenReturn("application/json; charset=utf-8");
        when(response.getResponse()).thenReturn("{}".getBytes(Charset.forName("UTF-8")));
        when(publishingClient.createUpdateContext(eq(serviceConfig), eq(endpointConfig), isNotNull())).thenReturn(updateContext);

        initialiseEndpoint = new InitialiseEndpoint(
            endpointClient,
            topicManagementClient,
            publishingClient,
            serviceConfig,
            serviceSession);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(endpointClient, topicManagementClient, serviceSession, response, publishingClient, updateContext);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void accept() {
        initialiseEndpoint.accept(endpointConfig);

        verify(endpointClient).request(eq(serviceConfig), eq(endpointConfig), isA(FutureCallback.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void acceptInfer() throws IOException {
        initialiseEndpoint.accept(inferEndpointConfig);

        verify(endpointClient).request(eq(serviceConfig), eq(inferEndpointConfig), callbackCaptor.capture());

        final FutureCallback<EndpointResponse> callback = callbackCaptor.getValue();
        callback.completed(response);

        verify(response, times(2)).getHeader("content-type");
        verify(response).getResponse();

        verify(topicManagementClient).addEndpoint(
            eq(serviceConfig),
            eq(endpointConfig),
            isA(AddCallback.class));
        verify(publishingClient).createUpdateContext(eq(serviceConfig), eq(endpointConfig), isNotNull());
        verify(updateContext).publish(isNotNull());
    }
}
