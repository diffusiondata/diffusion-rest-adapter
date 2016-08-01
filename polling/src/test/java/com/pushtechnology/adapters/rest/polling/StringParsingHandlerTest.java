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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import org.apache.http.concurrent.FutureCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

/**
 * Unit tests for {@link StringParsingHandler}.
 *
 * @author Push Technology Limited
 */
public final class StringParsingHandlerTest {
    @Mock
    private EndpointResponse endpointResponse;
    @Mock
    private FutureCallback<String> delegate;
    @Captor
    private ArgumentCaptor<String> stringCaptor;

    private FutureCallback<EndpointResponse> handler;

    @Before
    public void setUp() {
        initMocks(this);

        handler = new StringParsingHandler(delegate);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void completed() throws IOException {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain; charset=UTF-8");
        when(endpointResponse.getResponse()).thenReturn("{\"foo\":\"bar\"}".getBytes("UTF-8"));

        handler.completed(endpointResponse);
        verify(delegate).completed(stringCaptor.capture());

        final String json = stringCaptor.getValue();

        assertEquals("{\"foo\":\"bar\"}", json);
    }

    @Test
    public void completedCharsetMissing() throws IOException {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain");
        when(endpointResponse.getResponse()).thenReturn("{\"foo\":\"bar\"}".getBytes("ISO-8859-1"));

        handler.completed(endpointResponse);
        verify(delegate).completed(stringCaptor.capture());

        final String json = stringCaptor.getValue();

        assertEquals("{\"foo\":\"bar\"}", json);
    }

    @Test
    public void failed() {
        final Exception ex = new Exception("Intentional exception for test");
        handler.failed(ex);

        verify(delegate).failed(ex);
    }

    @Test
    public void cancelled() {
        handler.cancelled();

        verify(delegate).cancelled();
    }
}
