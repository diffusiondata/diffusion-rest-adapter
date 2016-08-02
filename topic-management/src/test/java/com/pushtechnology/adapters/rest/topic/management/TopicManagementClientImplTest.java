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

package com.pushtechnology.adapters.rest.topic.management;

import static com.pushtechnology.diffusion.client.topics.details.TopicType.BINARY;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.JSON;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.callbacks.TopicTreeHandler;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.session.Session;

/**
 * Unit tests for {@link TopicManagementClientImpl}.
 *
 * @author Push Technology Limited
 */
public final class TopicManagementClientImplTest {
    @Mock
    private Session session;
    @Mock
    private TopicControl topicControl;
    @Mock
    private TopicControl.AddCallback addCallback;
    @Captor
    private ArgumentCaptor<TopicControl.AddCallback> callbackCaptor;

    private final EndpointConfig jsonEndpointConfig = EndpointConfig
        .builder()
        .name("jsonEndpoint")
        .url("endpoint")
        .topic("jsonEndpoint")
        .produces("json")
        .build();
    private final EndpointConfig binaryEndpointConfig = EndpointConfig
        .builder()
        .name("binaryEndpoint")
        .url("endpoint")
        .topic("binaryEndpoint")
        .produces("binary")
        .build();
    private final EndpointConfig stringEndpointConfig = EndpointConfig
        .builder()
        .name("stringEndpoint")
        .url("endpoint")
        .topic("stringEndpoint")
        .produces("string")
        .build();
    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .host("localhost")
        .port(80)
        .pollPeriod(5000)
        .topicRoot("service")
        .endpoints(asList(jsonEndpointConfig, binaryEndpointConfig, stringEndpointConfig))
        .build();

    private TopicManagementClient topicManagementClient;

    @Before
    public void setUp() {
        initMocks(this);

        topicManagementClient = new TopicManagementClientImpl(session);

        when(session.feature(TopicControl.class)).thenReturn(topicControl);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(topicControl, addCallback);
    }

    @Test
    public void addService() {
        topicManagementClient.addService(serviceConfig);

        verify(topicControl).removeTopicsWithSession(eq("service"), isA(TopicTreeHandler.class));
    }

    @Test
    public void addJSONEndpoint() {
        topicManagementClient.addEndpoint(serviceConfig, jsonEndpointConfig, addCallback);

        verify(topicControl).addTopic(eq("service/jsonEndpoint"), eq(JSON), callbackCaptor.capture());

        callbackCaptor.getValue().onDiscard();

        verify(addCallback).onDiscard();
    }

    @Test
    public void addBinaryEndpoint() {
        topicManagementClient.addEndpoint(serviceConfig, binaryEndpointConfig, addCallback);

        verify(topicControl).addTopic(eq("service/binaryEndpoint"), eq(BINARY), callbackCaptor.capture());

        callbackCaptor.getValue().onDiscard();

        verify(addCallback).onDiscard();
    }

    @Test
    public void addStringEndpoint() {
        topicManagementClient.addEndpoint(serviceConfig, stringEndpointConfig, addCallback);

        verify(topicControl).addTopic(eq("service/stringEndpoint"), eq(BINARY), callbackCaptor.capture());

        callbackCaptor.getValue().onDiscard();

        verify(addCallback).onDiscard();
    }
}
