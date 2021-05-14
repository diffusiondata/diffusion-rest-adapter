/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Unit tests for {@link EndpointResponseImpl}.
 *
 * @author Push Technology Limited
 */
public final class EndpointResponseImplTest {
    @Mock
    private HttpResponse<byte[]> httpResponse;

    @Before
    public void setUp() throws IOException {
        initMocks(this);
    }

    @Test
    public void getResponseLength() throws IOException {
        final EndpointResponse response = createResponse();
        assertEquals(13, response.getResponseLength());
    }

    @Test
    public void getResponse() throws IOException {
        final EndpointResponse response = createResponse();
        assertEquals("Hello, world!", new String(response.getResponse(), StandardCharsets.UTF_8));
        assertEquals("Hello, world!", new String(response.getResponse(), StandardCharsets.UTF_8));
    }

    @Test
    public void getHeader() throws IOException {
        final EndpointResponse response = createResponse();
        assertEquals("text/plain", response.getHeader("Content-Type"));
    }

    @Test
    public void getStatusCode() throws IOException {
        final EndpointResponse response = createResponse();
        assertEquals(200, response.getStatusCode());
    }

    private EndpointResponse createResponse() throws IOException {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("Hello, world!".getBytes());
        when(httpResponse.headers()).thenReturn(HttpHeaders.of(Map.of("Content-Type", List.of("text/plain")), (a, b) -> true));
        return EndpointResponseImpl.create(httpResponse);
    }
}
