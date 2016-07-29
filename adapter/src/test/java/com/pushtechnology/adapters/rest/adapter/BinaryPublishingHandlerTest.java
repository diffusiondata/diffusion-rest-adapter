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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.diffusion.datatype.binary.Binary;

/**
 * Unit tests for {@link BinaryPublishingHandler}.
 *
 * @author Push Technology Limited
 */
public final class BinaryPublishingHandlerTest {
    @Mock
    private PublishingClient publishingClient;
    @Mock
    private Binary binary;

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

    private BinaryPublishingHandler pollHandler;

    @Before
    public void setUp() {
        initMocks(this);

        pollHandler = new BinaryPublishingHandler(publishingClient, serviceConfig, endpointConfig);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(publishingClient, binary);
    }

    @Test
    public void completed() {
        pollHandler.completed(binary);

        verify(publishingClient).publish(serviceConfig, endpointConfig, binary);
    }

    @Test
    public void failed() {
        pollHandler.failed(new Exception("Intentional for test"));
    }

    @Test
    public void cancelled() {
        pollHandler.cancelled();
    }
}
