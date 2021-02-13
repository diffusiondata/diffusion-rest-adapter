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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.hotspot.DefaultExports;

/**
 * Metrics reporter that can be scrapped by Prometheus.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class PrometheusReporter implements MetricsReporter {
    private static final Logger LOG = LoggerFactory.getLogger(PrometheusReporter.class);
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger(0);
    private static final String HEALTHY_RESPONSE = "Exporter is Healthy.";

    static {
        DefaultExports.initialize();
    }

    private final int port;

    @GuardedBy("this")
    private HttpServer server;
    @GuardedBy("this")
    private ExecutorService executorService;

    /**
     * Constructor.
     */
    public PrometheusReporter(int port) {
        this.port = port;
    }

    @Override
    public synchronized void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 3);
            executorService = Executors.newFixedThreadPool(1, new Threads());
            server.setExecutor(executorService);

            final HttpHandler metricHandler = new MetricsHandler();
            final HttpHandler healthHandler = new HealthHandler();

            server.createContext("/", metricHandler);
            server.createContext("/metrics", metricHandler);
            server.createContext("/-/healthy", healthHandler);

            server.start();
        }
        catch (IOException e) {
            LOG.warn("Failed to start Prometheus metrics exporter", e);
        }
    }

    @Override
    public synchronized void close() {
        if (server != null) {
            server.stop(0);
            executorService.shutdown();

            server = null;
            executorService = null;
        }
    }

    /**
     * Thread factory for the Prometheus metrics reporter.
     */
    @ThreadSafe
    /*package*/ static final class Threads implements ThreadFactory {
        @Override
        public Thread newThread(@NotNull Runnable r) {
            final Thread t = new Thread(r, "prometheus-exporter-" + THREAD_COUNT.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }

    /**
     * Health check handler for the Prometheus metrics reporter.
     */
    @ThreadSafe
    /*package*/ static final class HealthHandler implements HttpHandler {
        @GuardedBy("this")
        private final ByteArrayOutputStream response = new ByteArrayOutputStream(1 << 20);

        @Override
        public synchronized void handle(HttpExchange exchange) throws IOException {
            response.reset();

            final OutputStreamWriter writer = new OutputStreamWriter(response, StandardCharsets.UTF_8);
            writer.write(HEALTHY_RESPONSE);
            writer.close();

            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.size());
            response.writeTo(exchange.getResponseBody());
            exchange.close();
        }
    }

    /**
     * Metrics request for the Prometheus metrics reporter.
     */
    @ThreadSafe
    /*package*/ static final class MetricsHandler implements HttpHandler {
        @GuardedBy("this")
        private final ByteArrayOutputStream response = new ByteArrayOutputStream(256);

        @Override
        public synchronized void handle(HttpExchange exchange) throws IOException {
            response.reset();

            final OutputStreamWriter writer = new OutputStreamWriter(response, StandardCharsets.UTF_8);
            final Headers requestHeaders = exchange.getRequestHeaders();
            final String contentType = TextFormat.chooseContentType(requestHeaders.getFirst("Accept"));
            TextFormat.writeFormat(contentType, writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
            writer.close();

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.size());
            response.writeTo(exchange.getResponseBody());
            exchange.close();
        }
    }
}
