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

package com.pushtechnology.adapters.rest.topic.management;

import static com.pushtechnology.diffusion.client.topics.details.TopicSpecification.REMOVAL;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener.TopicCreationCompletionListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicSpecification;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.v4.SessionDisconnectedException;

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
    private TopicCreationListener topicCreationListener;
    @Mock
    private TopicCreationCompletionListener topicCreationCompletionListener;
    @Mock
    private TopicSpecification specification;

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

        when(topicCreationListener.onTopicCreationRequest(isNotNull(), isNotNull())).thenReturn(topicCreationCompletionListener);
        when(session.feature(TopicControl.class)).thenReturn(topicControl);
        when(session.getPrincipal()).thenReturn("adapter");
        when(topicControl.newSpecification(any())).thenReturn(specification);
        when(specification.withProperty(any(), any())).thenReturn(specification);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(topicControl, topicCreationListener, topicCreationCompletionListener, specification);
    }

    @Test
    public void addService() {
        topicManagementClient.addService(serviceConfig);
    }

    @Test
    public void addJSONEndpoint() {
        when(specification.getType()).thenReturn(TopicType.JSON);
        when(topicControl.addTopic(isNotNull(), ArgumentMatchers.<TopicSpecification>isNotNull())).thenReturn(cf(new SessionDisconnectedException()));

        topicManagementClient.addEndpoint(serviceConfig, jsonEndpointConfig);

        verify(topicCreationListener).onTopicCreationRequest("service/jsonEndpoint", TopicType.JSON);
        verify(topicControl).newSpecification(TopicType.JSON);
        verify(topicControl).addTopic("service/jsonEndpoint", specification);
        verify(specification).withProperty(REMOVAL, "when no session has \"$Principal eq 'adapter'\" for 1m");
        verify(specification).getType();

        verify(topicCreationCompletionListener).onTopicCreationFailed(TopicAddFailReason.UNEXPECTED_ERROR);
    }

    @Test
    public void addBinaryEndpoint() {
        when(specification.getType()).thenReturn(TopicType.BINARY);
        when(topicControl.addTopic(isNotNull(), ArgumentMatchers.<TopicSpecification>isNotNull())).thenReturn(cf(new SessionDisconnectedException()));

        topicManagementClient.addEndpoint(serviceConfig, binaryEndpointConfig);

        verify(topicCreationListener).onTopicCreationRequest("service/binaryEndpoint", TopicType.BINARY);
        verify(topicControl).newSpecification(TopicType.BINARY);
        verify(topicControl).addTopic("service/binaryEndpoint", specification);
        verify(specification).withProperty(REMOVAL, "when no session has \"$Principal eq 'adapter'\" for 1m");
        verify(specification).getType();

        verify(topicCreationCompletionListener).onTopicCreationFailed(TopicAddFailReason.UNEXPECTED_ERROR);
    }

    @Test
    public void addStringEndpoint() {
        when(specification.getType()).thenReturn(TopicType.STRING);
        when(topicControl.addTopic(isNotNull(), ArgumentMatchers.<TopicSpecification>isNotNull())).thenReturn(cf(new SessionDisconnectedException()));

        topicManagementClient.addEndpoint(serviceConfig, stringEndpointConfig);

        verify(topicCreationListener).onTopicCreationRequest("service/stringEndpoint", TopicType.STRING);
        verify(topicControl).newSpecification(TopicType.STRING);
        verify(topicControl).addTopic("service/stringEndpoint", specification);
        verify(specification).withProperty(REMOVAL, "when no session has \"$Principal eq 'adapter'\" for 1m");
        verify(specification).getType();

        verify(topicCreationCompletionListener).onTopicCreationFailed(TopicAddFailReason.UNEXPECTED_ERROR);
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
    }

    private static CompletableFuture<TopicControl.AddTopicResult> cf(Exception exception) {
        final CompletableFuture<TopicControl.AddTopicResult> result = new CompletableFuture<>();
        result.completeExceptionally(new CompletionException(exception));
        return result;
    }
}
