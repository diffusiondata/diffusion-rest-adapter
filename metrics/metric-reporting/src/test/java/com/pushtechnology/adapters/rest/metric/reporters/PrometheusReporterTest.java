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

package com.pushtechnology.adapters.rest.metric.reporters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.prometheus.client.Counter;

/**
 * Unit tests for {@link PrometheusReporter}.
 *
 * @author Push Technology Limited
 */

public final class PrometheusReporterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private HttpExchange exchange;

    @Mock
    private Headers headers;

    private ByteArrayOutputStream outputStream;

    @Before
    public void setup() {
        outputStream = new ByteArrayOutputStream();
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(outputStream);
        when(headers.getFirst("Accept")).thenReturn("text/plain; version=0.0.4; charset=utf-8");
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(exchange, headers);
    }

    @Test
    public void threadNames() {
        final ThreadFactory factory = new PrometheusReporter.Threads();
        final Thread thread = factory.newThread(() -> { });
        assertEquals("prometheus-exporter-0", thread.getName());
        assertTrue(thread.isDaemon());
    }

    @Test
    public void healthHandler() throws IOException {
        final HttpHandler handler = new PrometheusReporter.HealthHandler();

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(200, 20L);
        verify(headers).set("Content-Type", "text/plain");

        verify(exchange).getResponseBody();
        verify(exchange).getResponseHeaders();
        verify(exchange).close();

        assertEquals("Exporter is Healthy.", outputStream.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void metricHandler() throws IOException {
        Counter.build().name("test").help("A test").register();

        final HttpHandler handler = new PrometheusReporter.MetricsHandler();

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        verify(headers).getFirst("Accept");
        verify(headers).set("Content-Type", "text/plain; version=0.0.4; charset=utf-8");

        verify(exchange).getResponseBody();
        verify(exchange).getResponseHeaders();
        verify(exchange).getRequestHeaders();
        verify(exchange).close();

        final String response = outputStream.toString(StandardCharsets.UTF_8);

        assertThat(response, containsString("# HELP test_total A test"));
        assertThat(response, containsString("# TYPE test_total counter"));
        assertThat(response, containsString("test_total 0.0"));
    }
}
