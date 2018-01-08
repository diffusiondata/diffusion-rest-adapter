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

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.CancellationException;
import java.util.function.BiConsumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;

/**
 * Unit tests for {@link ValidateContentType}.
 *
 * @author Push Technology Limited
 */
public final class ValidateContentTypeTest {
    @Mock
    private BiConsumer<EndpointResponse, Throwable> delegate;
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

    private ValidateContentType validateJson;
    private ValidateContentType validatePlain;
    private ValidateContentType validateBinary;

    @Before
    public void setUp() {
        initMocks(this);

        validateJson = new ValidateContentType(jsonEndpoint, delegate);
        validatePlain = new ValidateContentType(plainEndpoint, delegate);
        validateBinary = new ValidateContentType(binaryEndpoint, delegate);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(delegate, endpointResponse);
    }

    @Test
    public void unknownContentTypeJson() {
        validateJson.accept(endpointResponse, null);

        verify(endpointResponse).getHeader("content-type");
        verify(delegate).accept(endpointResponse, null);
    }

    @Test
    public void unknownContentTypePlain() {
        validateJson.accept(endpointResponse, null);

        verify(endpointResponse).getHeader("content-type");
        verify(delegate).accept(endpointResponse, null);
    }

    @Test
    public void unknownContentTypeBinary() {
        validateJson.accept(endpointResponse, null);

        verify(endpointResponse).getHeader("content-type");
        verify(delegate).accept(endpointResponse, null);
    }

    @Test
    public void jsonContentTypeJson() {
        when(endpointResponse.getHeader("content-type")).thenReturn("application/json");
        validateJson.accept(endpointResponse, null);

        verify(endpointResponse).getHeader("content-type");
        verify(delegate).accept(endpointResponse, null);
    }

    @Test
    public void jsonContentTypePlain() {
        when(endpointResponse.getHeader("content-type")).thenReturn("application/json");
        validatePlain.accept(endpointResponse, null);

        verify(endpointResponse).getHeader("content-type");
        verify(delegate).accept(endpointResponse, null);
    }

    @Test
    public void jsonContentTypeBinary() {
        when(endpointResponse.getHeader("content-type")).thenReturn("application/json");
        validateBinary.accept(endpointResponse, null);

        verify(endpointResponse).getHeader("content-type");
        verify(delegate).accept(endpointResponse, null);
    }

    @Test
    public void plainContentTypeJson() {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain");
        validateJson.accept(endpointResponse, null);

        verify(endpointResponse).getHeader("content-type");
        verify(delegate).accept(isNull(), isA(Exception.class));
    }

    @Test
    public void plainContentTypePlain() {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain");
        validatePlain.accept(endpointResponse, null);

        verify(endpointResponse).getHeader("content-type");
        verify(delegate).accept(endpointResponse, null);
    }

    @Test
    public void plainContentTypeBinary() {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain");
        validateBinary.accept(endpointResponse, null);

        verify(endpointResponse).getHeader("content-type");
        verify(delegate).accept(endpointResponse, null);
    }

    @Test
    public void cancelled() {
        final Exception exception = new CancellationException();
        validateBinary.accept(null, exception);

        verify(delegate).accept(null, exception);
    }

    @Test
    public void failed() {
        final Exception exception = new Exception("Intentional for test");
        validateBinary.accept(null, exception);

        verify(delegate).accept(null, exception);
    }
}
