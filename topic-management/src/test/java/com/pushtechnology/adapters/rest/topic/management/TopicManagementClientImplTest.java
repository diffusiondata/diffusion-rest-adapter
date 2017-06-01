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

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.CompletableFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.callbacks.Registration;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.datatype.json.JSON;

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
    @Mock
    private JSON json;
    @Mock
    private Binary binary;
    @Mock
    private Registration registration;
    @Mock
    private TopicCreationListener topicCreationListener;
    @Captor
    private ArgumentCaptor<TopicControl.AddCallback> callbackCaptor;

    private final EndpointConfig jsonEndpointConfig = EndpointConfig
        .builder()
        .name("jsonEndpoint")
        .url("endpoint")
        .topicPath("jsonEndpoint")
        .produces("json")
        .build();
    private final EndpointConfig binaryEndpointConfig = EndpointConfig
        .builder()
        .name("binaryEndpoint")
        .url("endpoint")
        .topicPath("binaryEndpoint")
        .produces("binary")
        .build();
    private final EndpointConfig stringEndpointConfig = EndpointConfig
        .builder()
        .name("stringEndpoint")
        .url("endpoint")
        .topicPath("stringEndpoint")
        .produces("string")
        .build();
    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service")
        .host("localhost")
        .port(80)
        .pollPeriod(5000)
        .topicPathRoot("service")
        .endpoints(asList(jsonEndpointConfig, binaryEndpointConfig, stringEndpointConfig))
        .build();

    private TopicManagementClient topicManagementClient;

    @Before
    public void setUp() {
        initMocks(this);

        topicManagementClient = new TopicManagementClientImpl(topicCreationListener, session);

        when(session.feature(TopicControl.class)).thenReturn(topicControl);
        when(topicControl.removeTopicsWithSession(any())).thenReturn(CompletableFuture.completedFuture(registration));
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(topicControl, addCallback, topicCreationListener);
    }

    @Test
    public void addService() {
        topicManagementClient.addService(serviceConfig);

        verify(topicControl).removeTopicsWithSession(eq("service"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void addJSONEndpoint() {
        topicManagementClient.addEndpoint(serviceConfig, jsonEndpointConfig, addCallback);

        verify(topicCreationListener).onTopicCreationRequest(serviceConfig, jsonEndpointConfig);
        verify(topicControl).addTopic(eq("service/jsonEndpoint"), eq(TopicType.JSON), callbackCaptor.capture());

        callbackCaptor.getValue().onDiscard();

        verify(addCallback).onDiscard();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void addBinaryEndpoint() {
        topicManagementClient.addEndpoint(serviceConfig, binaryEndpointConfig, addCallback);

        verify(topicCreationListener).onTopicCreationRequest(serviceConfig, binaryEndpointConfig);
        verify(topicControl).addTopic(eq("service/binaryEndpoint"), eq(TopicType.BINARY), callbackCaptor.capture());

        callbackCaptor.getValue().onDiscard();

        verify(addCallback).onDiscard();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void addStringEndpoint() {
        topicManagementClient.addEndpoint(serviceConfig, stringEndpointConfig, addCallback);

        verify(topicCreationListener).onTopicCreationRequest(serviceConfig, stringEndpointConfig);
        verify(topicControl).addTopic(eq("service/stringEndpoint"), eq(TopicType.BINARY), callbackCaptor.capture());

        callbackCaptor.getValue().onDiscard();

        verify(addCallback).onDiscard();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void addJSONEndpointWithValue() {
        topicManagementClient.addEndpoint(serviceConfig, jsonEndpointConfig, json, addCallback);

        verify(topicCreationListener).onTopicCreationRequest(serviceConfig, jsonEndpointConfig, json);
        verify(topicControl)
            .addTopic(eq("service/jsonEndpoint"), eq(TopicType.JSON), eq(json), callbackCaptor.capture());

        callbackCaptor.getValue().onDiscard();

        verify(addCallback).onDiscard();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void addBinaryEndpointWithValue() {
        topicManagementClient.addEndpoint(serviceConfig, binaryEndpointConfig, binary, addCallback);

        verify(topicCreationListener).onTopicCreationRequest(serviceConfig, binaryEndpointConfig, binary);
        verify(topicControl)
            .addTopic(eq("service/binaryEndpoint"), eq(TopicType.BINARY), eq(binary), callbackCaptor.capture());

        callbackCaptor.getValue().onDiscard();

        verify(addCallback).onDiscard();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void addStringEndpointWithValue() {
        topicManagementClient.addEndpoint(serviceConfig, stringEndpointConfig, binary, addCallback);

        verify(topicCreationListener).onTopicCreationRequest(serviceConfig, stringEndpointConfig, binary);
        verify(topicControl)
            .addTopic(eq("service/stringEndpoint"), eq(TopicType.BINARY), isA(Binary.class), callbackCaptor.capture());

        callbackCaptor.getValue().onDiscard();

        verify(addCallback).onDiscard();
    }

    @Test
    public void removeEndpoint() {
        addJSONEndpoint();

        topicManagementClient.removeEndpoint(serviceConfig, jsonEndpointConfig);

        verify(topicControl).removeTopics("service/jsonEndpoint");
    }

    @Test
    public void removeService() {
        addService();

        topicManagementClient.removeService(serviceConfig);

        verify(registration).close();
    }
}
