/*******************************************************************************
 * Copyright (C) 2020 Push Technology Ltd.
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.TestHttpClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
    private TestHttpClient httpClient;
    @Mock
    private Future<HttpResponse> future;
    @Mock
    private PollListener pollListener;
    @Mock
    private PollCompletionListener completionListener;
    @Captor
    private ArgumentCaptor<FutureCallback<SimpleHttpResponse>> callbackCaptor;

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

    private EndpointClientImpl endpointClient;
    private SimpleHttpResponse response;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        initMocks(this);

        when(clientFactory.create(model, null)).thenReturn(httpClient);
        response = new SimpleHttpResponse(200);
        response.setBody("{}", ContentType.TEXT_PLAIN);
        response.addHeader("content-type", "text/plain");
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
        verify(httpClient).start();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void request() {
        endpointClient.start();

        verify(clientFactory).create(model, null);
        verify(httpClient).start();

        final Future<?> handle =  endpointClient.request(serviceConfig, endpointConfig);

        assertNotEquals(future, handle);
        verify(pollListener).onPollRequest(serviceConfig, endpointConfig);
        verify(httpClient).doExecute(any(), isNotNull(), isNotNull(), any(), isNotNull(), isNotNull());
    }

    @Test
    public void requestResponse() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        endpointClient.start();

        verify(clientFactory).create(model, null);
        verify(httpClient).start();

        final CompletableFuture<EndpointResponse> handle = endpointClient.request(serviceConfig, endpointConfig);

        assertNotEquals(future, handle);
        verify(pollListener).onPollRequest(serviceConfig, endpointConfig);
        verify(httpClient).doExecute(any(), isNotNull(), isNotNull(), any(), isNotNull(), callbackCaptor.capture());

        final FutureCallback<SimpleHttpResponse> responseCallback = callbackCaptor.getValue();

        responseCallback.completed(response);
        final EndpointResponse endpointResponse = handle.get(1, TimeUnit.SECONDS);
        verify(completionListener).onPollResponse(endpointResponse);
        final byte[] response = endpointResponse.getResponse();
        assertArrayEquals("{}".getBytes("UTF-8"), response);
    }

    @Test
    public void requestResponseFailed() throws InterruptedException, ExecutionException, TimeoutException {
        endpointClient.start();

        verify(clientFactory).create(model, null);
        verify(httpClient).start();

        final CompletableFuture<EndpointResponse> handle = endpointClient.request(serviceConfig, endpointConfig);

        assertNotEquals(future, handle);
        verify(pollListener).onPollRequest(serviceConfig, endpointConfig);
        verify(httpClient).doExecute(any(), isNotNull(), isNotNull(), any(), isNotNull(), callbackCaptor.capture());

        final FutureCallback<SimpleHttpResponse> responseCallback = callbackCaptor.getValue();

        final Exception exception = new Exception("Intentional for test");
        responseCallback.failed(exception);
        verify(completionListener).onPollFailure(exception);
        final ExecutionException thrown = assertThrows(
            ExecutionException.class,
            () -> handle.get(1, TimeUnit.SECONDS));
        assertEquals(thrown.getCause(), exception);
    }

    @Test
    public void requestResponseCancelled() throws InterruptedException, ExecutionException, TimeoutException {
        endpointClient.start();

        verify(clientFactory).create(model, null);
        verify(httpClient).start();

        final CompletableFuture<EndpointResponse> handle = endpointClient.request(serviceConfig, endpointConfig);

        assertNotEquals(future, handle);
        verify(pollListener).onPollRequest(serviceConfig, endpointConfig);
        verify(httpClient).doExecute(any(), isNotNull(), isNotNull(), any(), isNotNull(), callbackCaptor.capture());

        final FutureCallback<SimpleHttpResponse> responseCallback = callbackCaptor.getValue();

        responseCallback.cancelled();
        assertThrows(
            CancellationException.class,
            () -> handle.get(1, TimeUnit.SECONDS));
    }

    @Test(expected = IllegalStateException.class)
    public void requestBeforeStart() {
        endpointClient.request(serviceConfig, endpointConfig);
    }

    @Test
    public void close() throws IOException {
        endpointClient.start();

        verify(clientFactory).create(model, null);
        verify(httpClient).start();

        endpointClient.close();

        verify(httpClient).close();
    }
}
