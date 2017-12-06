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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metric.reporters.EventCountReporter;
import com.pushtechnology.adapters.rest.metric.reporters.EventSummaryReporter;
import com.pushtechnology.adapters.rest.metric.reporters.PollEventCounter;
import com.pushtechnology.adapters.rest.metric.reporters.PollEventQuerier;
import com.pushtechnology.adapters.rest.metric.reporters.PublicationEventCounter;
import com.pushtechnology.adapters.rest.metric.reporters.PublicationEventQuerier;
import com.pushtechnology.adapters.rest.metric.reporters.TopicCreationEventCounter;
import com.pushtechnology.adapters.rest.metric.reporters.TopicCreationEventQuerier;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPollEventCollector;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPublicationEventCollector;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedTopicCreationEventCollector;
import com.pushtechnology.adapters.rest.metrics.reporting.topics.TopicBasedMetricsReporter;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.SummaryConfig;
import com.pushtechnology.adapters.rest.model.latest.TopicConfig;
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
        Model model,
        ScheduledExecutorService executorService,
        MetricsDispatcher metricsDispatcher) {
        final SummaryConfig summaryConfig = model.getMetrics().getSummary();

        final List<Runnable> startTasks = new ArrayList<>();
        final List<Runnable> stopTasks = new ArrayList<>();

        if (model.getMetrics().isCounting()) {
            LOG.info("Enabling counting metrics reporting");

            final EventCountReporter reporter = createCountReporter(executorService, metricsDispatcher);

            startTasks.add(reporter::start);
            stopTasks.add(reporter::close);
        }

        if (summaryConfig != null) {
            setUpSummaryMetrics(
                executorService,
                metricsDispatcher,
                summaryConfig,
                startTasks,
                stopTasks);

        }

        final TopicConfig topicConfig = model.getMetrics().getTopic();
        if (topicConfig != null) {
            LOG.info("Enabling metrics topic reporting");

            final TopicBasedMetricsReporter reporter = createTopicReporter(
                diffusionSession,
                executorService,
                metricsDispatcher,
                topicConfig);

            startTasks.add(reporter::start);
            stopTasks.add(reporter::close);
        }

        return new MetricsProvider(
            () -> startTasks.forEach(Runnable::run),
            () -> stopTasks.forEach(Runnable::run));
    }

    private TopicBasedMetricsReporter createTopicReporter(
        Session diffusionSession,
        ScheduledExecutorService executorService,
        MetricsDispatcher metricsDispatcher,
        TopicConfig topicConfig) {

        final int eventBound = topicConfig.getEventBound();
        final PollEventCounter pollCounter = new PollEventCounter();
        final PublicationEventCounter publicationCounter = new PublicationEventCounter();
        final TopicCreationEventCounter topicCreationCounter = new TopicCreationEventCounter();
        final BoundedPollEventCollector pollCollector = new BoundedPollEventCollector(eventBound);
        final BoundedPublicationEventCollector publicationCollector = new BoundedPublicationEventCollector(
            eventBound);
        final BoundedTopicCreationEventCollector topicCreationCollector = new BoundedTopicCreationEventCollector(
            eventBound);
        final PollEventQuerier pollQuerier = new PollEventQuerier(pollCollector);
        final PublicationEventQuerier publicationQuerier = new PublicationEventQuerier(publicationCollector);
        final TopicCreationEventQuerier topicCreationQuerier =
            new TopicCreationEventQuerier(topicCreationCollector);

        final TopicBasedMetricsReporter metricsReporter = new TopicBasedMetricsReporter(
            diffusionSession,
            pollCounter,
            publicationCounter,
            topicCreationCounter,
            executorService,
            pollQuerier,
            publicationQuerier,
            topicCreationQuerier,
            topicConfig.getMetricsTopic());

        metricsDispatcher.addPollEventListener(pollCounter);
        metricsDispatcher.addPublicationEventListener(publicationCounter);
        metricsDispatcher.addTopicCreationEventListener(topicCreationCounter);
        metricsDispatcher.addPollEventListener(pollCollector);
        metricsDispatcher.addPublicationEventListener(publicationCollector);
        metricsDispatcher.addTopicCreationEventListener(topicCreationCollector);

        return metricsReporter;
    }

    private void setUpSummaryMetrics(
            ScheduledExecutorService executorService,
            MetricsDispatcher metricsDispatcher,
            SummaryConfig summaryConfig,
            List<Runnable> startTasks,
            List<Runnable> stopTasks) {
        final int eventBound = summaryConfig.getEventBound();
        LOG.info("Enabling summary metrics reporting. {} event bound", eventBound);

        final BoundedPollEventCollector pollCollector = new BoundedPollEventCollector(eventBound);
        final BoundedPublicationEventCollector publicationCollector = new BoundedPublicationEventCollector(
            eventBound);
        final BoundedTopicCreationEventCollector topicCreationCollector = new BoundedTopicCreationEventCollector(
            eventBound);
        final PollEventQuerier pollQuerier = new PollEventQuerier(pollCollector);
        final PublicationEventQuerier publicationQuerier = new PublicationEventQuerier(publicationCollector);
        final TopicCreationEventQuerier topicCreationQuerier =
            new TopicCreationEventQuerier(topicCreationCollector);
        final EventSummaryReporter reporter = new EventSummaryReporter(
            executorService,
            pollQuerier,
            publicationQuerier,
            topicCreationQuerier);

        startTasks.add(reporter::start);
        stopTasks.add(reporter::close);

        metricsDispatcher.addPollEventListener(pollCollector);
        metricsDispatcher.addPublicationEventListener(publicationCollector);
        metricsDispatcher.addTopicCreationEventListener(topicCreationCollector);
    }

    private EventCountReporter createCountReporter(
            ScheduledExecutorService executorService,
            MetricsDispatcher metricsDispatcher) {
        final PollEventCounter pollCounter = new PollEventCounter();
        final PublicationEventCounter publicationCounter = new PublicationEventCounter();
        final TopicCreationEventCounter topicCreationCounter = new TopicCreationEventCounter();

        metricsDispatcher.addPollEventListener(pollCounter);
        metricsDispatcher.addPublicationEventListener(publicationCounter);
        metricsDispatcher.addTopicCreationEventListener(topicCreationCounter);

        return new EventCountReporter(
            pollCounter,
            publicationCounter,
            topicCreationCounter,
            executorService);
    }
}
