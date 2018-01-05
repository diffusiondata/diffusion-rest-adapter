/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.function.Function;

import org.apache.http.concurrent.FutureCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.endpoints.EndpointType;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;

/**
 * Unit tests for {@link InferTopicType}.
 *
 * @author Push Technology Limited
 */
public final class InferTopicTypeTest {
    @Mock
    private FutureCallback<EndpointResponse> delegate;
    @Mock
    private EndpointResponse endpointResponse;
    @Mock
    private Function<EndpointType<?>, FutureCallback<EndpointResponse>> factory;

    private InferTopicType inferTopicType;

    @Before
    public void setUp() {
        initMocks(this);

        when(factory.apply(isA(EndpointType.class))).thenReturn(delegate);

        inferTopicType = new InferTopicType(factory);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(delegate, endpointResponse);
    }

    @Test
    public void unknownContentType() {
        inferTopicType.completed(endpointResponse);

        verify(endpointResponse).getHeader("content-type");
        verify(factory).apply(EndpointType.BINARY_ENDPOINT_TYPE);
        verify(delegate).completed(endpointResponse);
    }

    @Test
    public void jsonContentType() {
        when(endpointResponse.getHeader("content-type")).thenReturn("application/json");
        inferTopicType.completed(endpointResponse);

        verify(endpointResponse).getHeader("content-type");
        verify(factory).apply(EndpointType.JSON_ENDPOINT_TYPE);
        verify(delegate).completed(endpointResponse);
    }

    @Test
    public void plainContentType() {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain");
        inferTopicType.completed(endpointResponse);

        verify(endpointResponse).getHeader("content-type");
        verify(factory).apply(EndpointType.PLAIN_TEXT_ENDPOINT_TYPE);
        verify(delegate).completed(endpointResponse);
    }

    @Test
    public void binaryContentType() {
        when(endpointResponse.getHeader("content-type")).thenReturn("application/octet-stream");
        inferTopicType.completed(endpointResponse);

        verify(endpointResponse).getHeader("content-type");
        verify(factory).apply(EndpointType.BINARY_ENDPOINT_TYPE);
        verify(delegate).completed(endpointResponse);
    }

    @Test
    public void weirdContentType() {
        when(endpointResponse.getHeader("content-type")).thenReturn("who/knows");
        inferTopicType.completed(endpointResponse);

        verify(endpointResponse).getHeader("content-type");
        verify(factory).apply(EndpointType.BINARY_ENDPOINT_TYPE);
        verify(delegate).completed(endpointResponse);
    }

    @Test
    public void cancelled() {
        inferTopicType.cancelled();

        verify(factory).apply(EndpointType.BINARY_ENDPOINT_TYPE);
        verify(delegate).cancelled();
    }

    @Test
    public void failed() {
        final Exception exception = new Exception("Intentional for test");
        inferTopicType.failed(exception);

        verify(factory).apply(EndpointType.BINARY_ENDPOINT_TYPE);
        verify(delegate).failed(exception);
    }
}
