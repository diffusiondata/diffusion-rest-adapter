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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metric.reporters.EventCountReporter;
import com.pushtechnology.adapters.rest.metric.reporters.EventSummaryReporter;
import com.pushtechnology.adapters.rest.metric.reporters.PollEventCounter;
import com.pushtechnology.adapters.rest.metric.reporters.PollEventQuerier;
import com.pushtechnology.adapters.rest.metric.reporters.PrometheusMetricsListener;
import com.pushtechnology.adapters.rest.metric.reporters.PrometheusReporter;
import com.pushtechnology.adapters.rest.metric.reporters.PublicationEventCounter;
import com.pushtechnology.adapters.rest.metric.reporters.PublicationEventQuerier;
import com.pushtechnology.adapters.rest.metric.reporters.TopicBasedMetricsReporter;
import com.pushtechnology.adapters.rest.metric.reporters.TopicCreationEventCounter;
import com.pushtechnology.adapters.rest.metric.reporters.TopicCreationEventQuerier;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPollEventCollector;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPublicationEventCollector;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedTopicCreationEventCollector;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.PrometheusConfig;
import com.pushtechnology.adapters.rest.model.latest.SummaryConfig;
import com.pushtechnology.adapters.rest.model.latest.TopicConfig;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.diffusion.client.session.Session;

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
        Session diffusionSession,
        TopicManagementClient topicManagementClient,
        PublishingClient publishingClient,
        Model model,
        ScheduledExecutorService executorService,
        MetricsDispatcher metricsDispatcher) {
        final SummaryConfig summaryConfig = model.getMetrics().getSummary();

        final List<Runnable> startTasks = new ArrayList<>();
        final List<Runnable> stopTasks = new ArrayList<>();

        final Helper helper = new Helper(metricsDispatcher);

        configureSimpleMetricsReporter(model, executorService, startTasks, stopTasks, helper);
        configureSummaryMetricsReporter(executorService, summaryConfig, startTasks, stopTasks, helper);
        configureTopicMetricsReporter(
            diffusionSession,
            topicManagementClient,
            publishingClient,
            model,
            executorService,
            startTasks,
            stopTasks,
            helper);
        configurePrometheusMetricsReporter(model, metricsDispatcher, startTasks, stopTasks);

        return new MetricsProvider(
            () -> startTasks.forEach(Runnable::run),
            () -> stopTasks.forEach(Runnable::run));
    }

    private void configureSimpleMetricsReporter(
            Model model,
            ScheduledExecutorService executorService,
            List<Runnable> startTasks,
            List<Runnable> stopTasks,
            Helper helper) {
        if (model.getMetrics().isCounting()) {
            LOG.info("Enabling counting metrics reporting");

            final EventCountReporter reporter = new EventCountReporter(
                helper.getPollEventCounter(),
                helper.getPublicationEventCounter(),
                helper.getTopicCreationEventCounter(),
                executorService);

            startTasks.add(reporter::start);
            stopTasks.add(reporter::close);
        }
    }

    private void configureSummaryMetricsReporter(
            ScheduledExecutorService executorService,
            SummaryConfig summaryConfig,
            List<Runnable> startTasks,
            List<Runnable> stopTasks,
            Helper helper) {
        if (summaryConfig != null) {
            final int eventBound = summaryConfig.getEventBound();
            LOG.info("Enabling summary metrics reporting. {} event bound", eventBound);

            final EventSummaryReporter reporter = new EventSummaryReporter(
                executorService,
                helper.getPollEventQuerier(eventBound),
                helper.getPublicationEventQuerier(eventBound),
                helper.getTopicCreationEventQuerier(eventBound));

            startTasks.add(reporter::start);
            stopTasks.add(reporter::close);
        }
    }

    // CHECKSTYLE.OFF: ParameterNumber
    private void configureTopicMetricsReporter(
    // CHECKSTYLE.ON
            Session diffusionSession,
            TopicManagementClient topicManagementClient,
            PublishingClient publishingClient,
            Model model,
            ScheduledExecutorService executorService,
            List<Runnable> startTasks,
            List<Runnable> stopTasks,
            Helper helper) {
        final TopicConfig topicConfig = model.getMetrics().getTopic();
        if (topicConfig != null) {
            final int eventBound = topicConfig.getEventBound();
            LOG.info("Enabling metrics topic reporting. {} event bound", eventBound);

            final TopicBasedMetricsReporter reporter = new TopicBasedMetricsReporter(
                diffusionSession,
                topicManagementClient,
                publishingClient,
                helper.getPollEventCounter(),
                helper.getPublicationEventCounter(),
                helper.getTopicCreationEventCounter(),
                executorService,
                helper.getPollEventQuerier(eventBound),
                helper.getPublicationEventQuerier(eventBound),
                helper.getTopicCreationEventQuerier(eventBound),
                topicConfig.getMetricsTopic());

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

            startTasks.add(reporter::start);
            stopTasks.add(reporter::close);
        }
    }

    /**
     * Helper class for the lazy creation of counters and queriers.
     */
    private static final class Helper {
        private final Map<Integer, PollEventQuerier> pollEventQueriers;
        private final Map<Integer, PublicationEventQuerier> publicationEventQueriers;
        private final Map<Integer, TopicCreationEventQuerier> topicCreationEventQueriers;
        private final MetricsDispatcher metricsDispatcher;
        private PollEventCounter pollEventCounter;
        private PublicationEventCounter publicationEventCounter;
        private TopicCreationEventCounter topicCreationEventCounter;

        Helper(MetricsDispatcher metricsDispatcher) {
            this.metricsDispatcher = metricsDispatcher;
            pollEventQueriers = new HashMap<>();
            publicationEventQueriers = new HashMap<>();
            topicCreationEventQueriers = new HashMap<>();
        }

        PollEventQuerier getPollEventQuerier(int eventBound) {
            return pollEventQueriers.computeIfAbsent(eventBound, bound -> {
                final BoundedPollEventCollector collector = new BoundedPollEventCollector(bound);
                metricsDispatcher.addPollEventListener(collector);
                return new PollEventQuerier(collector);
            });
        }

        PublicationEventQuerier getPublicationEventQuerier(int eventBound) {
            return publicationEventQueriers.computeIfAbsent(eventBound, bound -> {
                final BoundedPublicationEventCollector collector = new BoundedPublicationEventCollector(bound);
                metricsDispatcher.addPublicationEventListener(collector);
                return new PublicationEventQuerier(collector);
            });
        }

        TopicCreationEventQuerier getTopicCreationEventQuerier(int eventBound) {
            return topicCreationEventQueriers.computeIfAbsent(eventBound, bound -> {
                final BoundedTopicCreationEventCollector collector = new BoundedTopicCreationEventCollector(bound);
                metricsDispatcher.addTopicCreationEventListener(collector);
                return new TopicCreationEventQuerier(collector);
            });
        }


        PollEventCounter getPollEventCounter() {
            if (pollEventCounter == null) {
                pollEventCounter = new PollEventCounter();
                metricsDispatcher.addPollEventListener(pollEventCounter);
            }
            return pollEventCounter;
        }

        PublicationEventCounter getPublicationEventCounter() {
            if (publicationEventCounter == null) {
                publicationEventCounter = new PublicationEventCounter();
                metricsDispatcher.addPublicationEventListener(publicationEventCounter);
            }
            return publicationEventCounter;
        }

        TopicCreationEventCounter getTopicCreationEventCounter() {
            if (topicCreationEventCounter == null) {
                topicCreationEventCounter = new TopicCreationEventCounter();
                metricsDispatcher.addTopicCreationEventListener(topicCreationEventCounter);
            }
            return topicCreationEventCounter;
        }
    }
}
