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
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;

import kotlin.Pair;

/**
 * Unit tests for {@link ValidateContentType}.
 *
 * @author Push Technology Limited
 */
public final class ValidateContentTypeTest {
    @Mock
    private EndpointResponse endpointResponse;

    private EndpointConfig jsonEndpoint = EndpointConfig
        .builder()
        .name("endpoint")
        .topicPath("path")
        .url("url")
        .produces("application/json")
        .build();
    private EndpointConfig plainEndpoint = EndpointConfig
        .builder()
        .name("endpoint")
        .topicPath("path")
        .url("url")
        .produces("text/plain")
        .build();
    private EndpointConfig binaryEndpoint = EndpointConfig
        .builder()
        .name("endpoint")
        .topicPath("path")
        .url("url")
        .produces("binary")
        .build();

    private ValidateContentType validate;

    @Before
    public void setUp() {
        initMocks(this);

        validate = new ValidateContentType();
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(endpointResponse);
    }

    @Test
    public void unknownContentTypeJson() {
        validate.apply(new Pair<>(jsonEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
    }

    @Test
    public void unknownContentTypePlain() {
        validate.apply(new Pair<>(plainEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
    }

    @Test
    public void unknownContentTypeBinary() {
        validate.apply(new Pair<>(binaryEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
    }

    @Test
    public void jsonContentTypeJson() {
        when(endpointResponse.getHeader("content-type")).thenReturn("application/json");
        validate.apply(new Pair<>(jsonEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
    }

    @Test
    public void jsonContentTypePlain() {
        when(endpointResponse.getHeader("content-type")).thenReturn("application/json");
        validate.apply(new Pair<>(plainEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
    }

    @Test
    public void jsonContentTypeBinary() {
        when(endpointResponse.getHeader("content-type")).thenReturn("application/json");
        validate.apply(new Pair<>(binaryEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
    }

    @Test(expected = IllegalArgumentException.class)
    public void plainContentTypeJson() {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain");
        try {
            validate.apply(new Pair<>(jsonEndpoint, endpointResponse));
        }
        finally {
            verify(endpointResponse).getHeader("content-type");
        }
    }

    @Test
    public void plainContentTypePlain() {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain");
        validate.apply(new Pair<>(plainEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
    }

    @Test
    public void plainContentTypeBinary() {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain");
        validate.apply(new Pair<>(binaryEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
    }
}
