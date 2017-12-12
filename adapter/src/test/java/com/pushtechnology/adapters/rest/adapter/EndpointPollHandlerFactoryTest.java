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

import static com.pushtechnology.diffusion.client.Diffusion.dataTypes;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.BINARY;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.JSON;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.STRING;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.apache.http.concurrent.FutureCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.adapters.rest.polling.PollHandlerFactory;
import com.pushtechnology.adapters.rest.publication.PublishingClient;

/**
 * Unit tests for {@link EndpointPollHandlerFactoryImpl}.
 *
 * @author Push Technology Limited
 */
public final class EndpointPollHandlerFactoryTest {
    @Mock
    private PublishingClient publishingClient;

    private final EndpointConfig jsonEndpoint = EndpointConfig
        .builder()
        .name("endpoint-0")
        .topicPath("path")
        .url("/a/url/json")
        .produces("json")
        .build();
    private final EndpointConfig binaryEndpoint = EndpointConfig
        .builder()
        .name("endpoint-1")
        .topicPath("path")
        .url("/a/url/binary")
        .produces("binary")
        .build();
    private final EndpointConfig plainTextEndpoint = EndpointConfig
        .builder()
        .name("endpoint-2")
        .topicPath("path")
        .url("/a/url/text")
        .produces("text/plain")
        .build();
    private final EndpointConfig xmlEndpoint = EndpointConfig
        .builder()
        .name("endpoint-3")
        .topicPath("path")
        .url("/a/url/text")
        .produces("text/xml")
        .build();
    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service")
        .host("localhost")
        .port(80)
        .pollPeriod(5000L)
        .topicPathRoot("topic")
        .endpoints(asList(jsonEndpoint, binaryEndpoint, plainTextEndpoint, xmlEndpoint))
        .build();

    private PollHandlerFactory<EndpointResponse> pollHandlerFactory;

    @Before
    public void setUp() {
        initMocks(this);

        pollHandlerFactory = new EndpointPollHandlerFactoryImpl(publishingClient);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(publishingClient);
    }

    @Test
    public void createJson() {
        final FutureCallback<EndpointResponse> callback = pollHandlerFactory.create(serviceConfig, jsonEndpoint);

        assertTrue(callback instanceof TransformingHandler);
        verify(publishingClient).createUpdateContext(serviceConfig, jsonEndpoint, JSON, dataTypes().json());
    }

    @Test
    public void createBinary() {
        final FutureCallback<EndpointResponse> callback = pollHandlerFactory.create(serviceConfig, binaryEndpoint);

        assertTrue(callback instanceof TransformingHandler);
        verify(publishingClient).createUpdateContext(serviceConfig, binaryEndpoint, BINARY, dataTypes().binary());
    }

    @Test
    public void createPlainText() {
        final FutureCallback<EndpointResponse> callback = pollHandlerFactory.create(serviceConfig, plainTextEndpoint);

        assertTrue(callback instanceof TransformingHandler);
        verify(publishingClient).createUpdateContext(serviceConfig, plainTextEndpoint, STRING, dataTypes().string());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createXML() {
        pollHandlerFactory.create(serviceConfig, xmlEndpoint);
    }
}
