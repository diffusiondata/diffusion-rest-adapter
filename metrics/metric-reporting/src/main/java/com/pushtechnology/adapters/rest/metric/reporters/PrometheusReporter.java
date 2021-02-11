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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

/**
 * Metrics reporter that can be scrapped by Prometheus.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class PrometheusReporter implements MetricsReporter {
    private static final Logger LOG = LoggerFactory.getLogger(PrometheusReporter.class);

    static {
        DefaultExports.initialize();
    }

    private final int port;

    @GuardedBy("this")
    private HTTPServer exporter;

    /**
     * Constructor.
     */
    public PrometheusReporter(int port) {
        this.port = port;
    }

    @Override
    public synchronized void start() {
        try {
            exporter = new HTTPServer(port, true);
        }
        catch (IOException e) {
            LOG.error("Failed to start Prometheus metrics exporter", e);
        }
    }

    @Override
    public synchronized void close() {
        if (exporter != null) {
            exporter.stop();
        }
    }
}
