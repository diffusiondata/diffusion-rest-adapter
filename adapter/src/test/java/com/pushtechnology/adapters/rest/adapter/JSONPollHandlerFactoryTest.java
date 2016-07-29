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

import static java.util.Collections.singletonList;
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
import com.pushtechnology.adapters.rest.polling.PollHandlerFactory;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link JSONPollHandlerFactory}.
 *
 * @author Push Technology Limited
 */
public final class JSONPollHandlerFactoryTest {
    @Mock
    private JSON json;
    @Mock
    private PublishingClient publishingClient;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint-0")
        .topic("topic")
        .url("http://localhost/json")
        .build();

    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .host("localhost")
        .port(8080)
        .pollPeriod(60000)
        .endpoints(singletonList(endpointConfig))
        .topicRoot("a")
        .build();

    private PollHandlerFactory pollHandlerFactory;

    @Before
    public void setUp() {
        initMocks(this);

        pollHandlerFactory = new JSONPollHandlerFactory(publishingClient);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(publishingClient, json);
    }

    @Test
    public void create() {
        final FutureCallback<JSON> callback = pollHandlerFactory.create(serviceConfig, endpointConfig);

        callback.completed(json);
        verify(publishingClient).publish(serviceConfig, endpointConfig, json);
    }
}
