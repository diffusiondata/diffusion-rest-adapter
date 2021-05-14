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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.metrics.listeners.PollListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PollListener.PollCompletionListener;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.MetricsConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Unit tests for {@link EndpointClientImpl}.
 *
 * @author Push Technology Limited
 */
public final class EndpointClientImplTest {
    @Mock
    private HttpClientFactory clientFactory;
    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse<byte[]> response;
    @Mock
    private PollListener pollListener;
    @Mock
    private PollCompletionListener completionListener;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint")
        .url("/a/url.json")
        .produces("json")
        .topicPath("url")
        .build();
    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service")
        .host("localhost")
        .port(8080)
        .endpoints(singletonList(endpointConfig))
        .topicPathRoot("test")
        .build();
    private final Model model = Model
        .builder()
        .diffusion(DiffusionConfig
            .builder()
            .host("example.com")
            .build())
        .services(singletonList(serviceConfig))
        .metrics(MetricsConfig
            .builder()
            .build())
        .build();

    private CompletableFuture<HttpResponse<byte[]>> future;
    private EndpointClientImpl endpointClient;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws IOException {
        initMocks(this);

        future = new CompletableFuture<>();

        when(clientFactory.create(model, null)).thenReturn(httpClient);
        when(httpClient.sendAsync(isA(HttpRequest.class), isA(HttpResponse.BodyHandler.class))).thenReturn(future);
        when(response.body()).thenReturn("{}".getBytes(StandardCharsets.UTF_8));
        when(response.headers()).thenReturn(HttpHeaders.of(Map.of("Content-Type", List.of("application/json")), (a, b) -> true));
        when(response.statusCode()).thenReturn(200);
        when(pollListener.onPollRequest(serviceConfig, endpointConfig)).thenReturn(completionListener);

        endpointClient = new EndpointClientImpl(model, null, clientFactory, pollListener);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(httpClient, pollListener, completionListener);
    }

    @Test
    public void start() {
        endpointClient.start();

        verify(clientFactory).create(model, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void request() {
        endpointClient.start();

        verify(clientFactory).create(model, null);

        final Future<?> handle =  endpointClient.request(serviceConfig, endpointConfig);

        assertNotEquals(future, handle);
        verify(pollListener).onPollRequest(serviceConfig, endpointConfig);
        verify(httpClient).sendAsync(isA(HttpRequest.class), isA(HttpResponse.BodyHandler.class));
    }

    @Test
    public void requestResponse() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        future.complete(response);

        endpointClient.start();

        verify(clientFactory).create(model, null);

        final CompletableFuture<EndpointResponse> handle = endpointClient.request(serviceConfig, endpointConfig);

        verify(pollListener).onPollRequest(serviceConfig, endpointConfig);
        verify(httpClient).sendAsync(isA(HttpRequest.class), isA(HttpResponse.BodyHandler.class));

        final EndpointResponse endpointResponse = handle.get(1, TimeUnit.SECONDS);
        verify(completionListener).onPollResponse(endpointResponse);
        final byte[] response = endpointResponse.getResponse();
        assertArrayEquals("{}".getBytes("UTF-8"), response);
    }

    @Test
    public void requestResponseFailed() {
        final Exception exception = new Exception("Intentional for test");
        future.completeExceptionally(exception);

        endpointClient.start();

        verify(clientFactory).create(model, null);

        final CompletableFuture<EndpointResponse> handle = endpointClient.request(serviceConfig, endpointConfig);

        assertNotEquals(future, handle);
        verify(pollListener).onPollRequest(serviceConfig, endpointConfig);
        verify(httpClient).sendAsync(isA(HttpRequest.class), isA(HttpResponse.BodyHandler.class));

        verify(completionListener).onPollFailure(exception);
        final ExecutionException thrown = assertThrows(
            ExecutionException.class,
            () -> handle.get(1, TimeUnit.SECONDS));
        assertEquals(thrown.getCause(), exception);
    }

    @Test(expected = IllegalStateException.class)
    public void requestBeforeStart() {
        endpointClient.request(serviceConfig, endpointConfig);
    }

    @Test
    public void close() throws IOException {
        endpointClient.start();

        verify(clientFactory).create(model, null);

        endpointClient.close();
    }
}
