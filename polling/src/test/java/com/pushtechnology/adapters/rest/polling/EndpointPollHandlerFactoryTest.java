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

package com.pushtechnology.adapters.rest.polling;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Unit tests for {@link EndpointPollHandlerFactoryImpl}.
 *
 * @author Push Technology Limited
 */
public final class EndpointPollHandlerFactoryTest {
    @Mock
    private JSONPollHandlerFactory jsonPollHandlerFactory;
    @Mock
    private BinaryPollHandlerFactory binaryPollHandlerFactory;
    @Mock
    private StringPollHandlerFactory stringPollHandlerFactory;

    private final EndpointConfig jsonEndpoint = EndpointConfig
        .builder()
        .url("/a/url/json")
        .produces("json")
        .build();
    private final EndpointConfig binaryEndpoint = EndpointConfig
        .builder()
        .url("/a/url/binary")
        .produces("binary")
        .build();
    private final EndpointConfig plainTextEndpoint = EndpointConfig
        .builder()
        .url("/a/url/text")
        .produces("text/plain")
        .build();
    private final EndpointConfig xmlEndpoint = EndpointConfig
        .builder()
        .url("/a/url/text")
        .produces("text/xml")
        .build();
    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .host("localhost")
        .port(80)
        .pollPeriod(5000L)
        .endpoints(asList(jsonEndpoint, binaryEndpoint, plainTextEndpoint, xmlEndpoint))
        .build();

    private PollHandlerFactory<EndpointResponse> pollHandlerFactory;

    @Before
    public void setUp() {
        initMocks(this);

        pollHandlerFactory = new EndpointPollHandlerFactoryImpl(
            jsonPollHandlerFactory,
            binaryPollHandlerFactory,
            stringPollHandlerFactory);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(jsonPollHandlerFactory, binaryPollHandlerFactory, stringPollHandlerFactory);
    }

    @Test
    public void createJson() {
        pollHandlerFactory.create(serviceConfig, jsonEndpoint);

        verify(jsonPollHandlerFactory).create(serviceConfig, jsonEndpoint);
    }

    @Test
    public void createBinary() {
        pollHandlerFactory.create(serviceConfig, binaryEndpoint);

        verify(binaryPollHandlerFactory).create(serviceConfig, binaryEndpoint);
    }

    @Test
    public void createPlainText() {
        pollHandlerFactory.create(serviceConfig, plainTextEndpoint);

        verify(stringPollHandlerFactory).create(serviceConfig, plainTextEndpoint);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createXML() {
        pollHandlerFactory.create(serviceConfig, xmlEndpoint);
    }
}
