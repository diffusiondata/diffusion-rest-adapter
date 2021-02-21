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

package com.pushtechnology.adapters.rest.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metric.reporters.LogReporter;
import com.pushtechnology.adapters.rest.metric.reporters.PollEventCounter;
import com.pushtechnology.adapters.rest.metric.reporters.PrometheusMetricsListener;
import com.pushtechnology.adapters.rest.metric.reporters.PrometheusReporter;
import com.pushtechnology.adapters.rest.metric.reporters.PublicationEventCounter;
import com.pushtechnology.adapters.rest.metric.reporters.TopicCreationEventCounter;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.PrometheusConfig;

/**
 * Factory for {@link MetricsProvider}.
 *
 * @author Push Technology Limited
 */
public final class MetricsProviderFactory {
    private static final Logger LOG = LoggerFactory.getLogger(MetricsProviderFactory.class);

    /**
     * @return a new metrics provider
     */
    public MetricsProvider create(
        Model model,
        ScheduledExecutorService executorService,
        MetricsDispatcher metricsDispatcher) {

        final List<Runnable> startTasks = new ArrayList<>();
        final List<Runnable> stopTasks = new ArrayList<>();

        configureSimpleMetricsReporter(model, metricsDispatcher, executorService, startTasks, stopTasks);
        configurePrometheusMetricsReporter(model, metricsDispatcher, startTasks, stopTasks);

        return new MetricsProvider(
            () -> startTasks.forEach(Runnable::run),
            () -> stopTasks.forEach(Runnable::run));
    }

    private void configureSimpleMetricsReporter(
            Model model,
            MetricsDispatcher metricsDispatcher,
            ScheduledExecutorService executorService,
            List<Runnable> startTasks,
            List<Runnable> stopTasks) {
        if (model.getMetrics().isLogging()) {
            LOG.info("Enabling counting metrics reporting");

            final PollEventCounter pollEventCounter = new PollEventCounter();
            final PublicationEventCounter publicationEventCounter = new PublicationEventCounter();
            final TopicCreationEventCounter topicCreationEventCounter = new TopicCreationEventCounter();

            metricsDispatcher.addPollEventListener(pollEventCounter);
            metricsDispatcher.addPublicationEventListener(publicationEventCounter);
            metricsDispatcher.addTopicCreationEventListener(topicCreationEventCounter);

            final LogReporter reporter = new LogReporter(
                pollEventCounter,
                publicationEventCounter,
                topicCreationEventCounter,
                executorService);

            startTasks.add(reporter::start);
            stopTasks.add(reporter::close);
        }
    }

    private void configurePrometheusMetricsReporter(
            Model model,
            MetricsDispatcher metricsDispatcher,
            List<Runnable> startTasks,
            List<Runnable> stopTasks) {
        final PrometheusConfig prometheusConfig = model.getMetrics().getPrometheus();
        if (prometheusConfig != null) {
            LOG.info("Enabling metrics prometheus reporting");

            final PrometheusMetricsListener listener = new PrometheusMetricsListener();
            final PrometheusReporter reporter = new PrometheusReporter(prometheusConfig.getPort());

            metricsDispatcher.addPollEventListener(listener);
            metricsDispatcher.addPublicationEventListener(listener);
            metricsDispatcher.addTopicCreationEventListener(listener);
            metricsDispatcher.addServiceEventListener(listener);

            startTasks.add(reporter::start);
            stopTasks.add(reporter::close);
        }
    }
}
