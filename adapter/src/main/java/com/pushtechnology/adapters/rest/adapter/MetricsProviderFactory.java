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

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.injectors.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metric.reporters.EventCountReporter;
import com.pushtechnology.adapters.rest.metric.reporters.EventSummaryReporter;
import com.pushtechnology.adapters.rest.metric.reporters.PollEventQuerier;
import com.pushtechnology.adapters.rest.metric.reporters.PublicationEventQuerier;
import com.pushtechnology.adapters.rest.metric.reporters.TopicCreationEventQuerier;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPollEventCollector;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPublicationEventCollector;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedTopicCreationEventCollector;
import com.pushtechnology.adapters.rest.metrics.event.listeners.PollEventDispatcher;
import com.pushtechnology.adapters.rest.metrics.event.listeners.PublicationEventDispatcher;
import com.pushtechnology.adapters.rest.metrics.event.listeners.TopicCreationEventDispatcher;
import com.pushtechnology.adapters.rest.metrics.listeners.DelegatingPollListener;
import com.pushtechnology.adapters.rest.metrics.listeners.DelegatingPublicationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.DelegatingTopicCreationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PollEventCounter;
import com.pushtechnology.adapters.rest.metrics.listeners.PollListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationEventCounter;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationEventCounter;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.SummaryConfig;

/**
 * Factory for {@link MetricsProvider}.
 *
 * @author Push Technology Limited
 */
public final class MetricsProviderFactory implements Provider {
    private static final Logger LOG = LoggerFactory.getLogger(MetricsProviderFactory.class);

    /**
     * @return a new metrics provider
     */
    public MetricsProvider provide(Model model, ScheduledExecutorService executorService) {
        final SummaryConfig summaryConfig = model.getMetrics().getSummary();

        final List<Runnable> startTasks = new ArrayList<>();
        final List<Runnable> stopTasks = new ArrayList<>();
        final List<PollListener> delegatePollListeners = new ArrayList<>();
        final List<PublicationListener> delegatePublicationListeners = new ArrayList<>();
        final List<TopicCreationListener> delegateTopicCreationListeners = new ArrayList<>();

        if (model.getMetrics().isCounting()) {
            LOG.info("Enabling counting metrics reporting.");

            final MutablePicoContainer factoryContainer = new PicoBuilder()
                .withCaching()
                .withConstructorInjection()
                .withJavaEE5Lifecycle()
                .withLocking()
                .build()
                .addComponent(executorService)
                .addComponent(PollEventCounter.class)
                .addComponent(PublicationEventCounter.class)
                .addComponent(TopicCreationEventCounter.class)
                .addComponent(EventCountReporter.class);

            startTasks.add(factoryContainer.getComponent(EventCountReporter.class)::start);
            stopTasks.add(factoryContainer.getComponent(EventCountReporter.class)::close);
            delegatePollListeners.add(factoryContainer.getComponent(PollListener.class));
            delegatePublicationListeners.add(factoryContainer.getComponent(PublicationListener.class));
            delegateTopicCreationListeners.add(factoryContainer.getComponent(TopicCreationListener.class));
        }

        if (summaryConfig != null) {
            final int eventBound = summaryConfig.getEventBound();
            LOG.info("Enabling summary metrics reporting. {} event bound", eventBound);

            final MutablePicoContainer factoryContainer = new PicoBuilder()
                .withCaching()
                .withConstructorInjection()
                .withJavaEE5Lifecycle()
                .withLocking()
                .build()
                .addComponent(executorService)
                .addComponent(PollEventDispatcher.class)
                .addComponent(PublicationEventDispatcher.class)
                .addComponent(TopicCreationEventDispatcher.class)
                .addComponent(new BoundedPollEventCollector(eventBound))
                .addComponent(new BoundedPublicationEventCollector(eventBound))
                .addComponent(new BoundedTopicCreationEventCollector(eventBound))
                .addComponent(PollEventQuerier.class)
                .addComponent(PublicationEventQuerier.class)
                .addComponent(TopicCreationEventQuerier.class)
                .addComponent(EventSummaryReporter.class);

            startTasks.add(factoryContainer.getComponent(EventSummaryReporter.class)::start);
            stopTasks.add(factoryContainer.getComponent(EventSummaryReporter.class)::close);
            delegatePollListeners.add(factoryContainer.getComponent(PollListener.class));
            delegatePublicationListeners.add(factoryContainer.getComponent(PublicationListener.class));
            delegateTopicCreationListeners.add(factoryContainer.getComponent(TopicCreationListener.class));
        }

        return new MetricsProvider(
            () -> startTasks.forEach(Runnable::run),
            () -> stopTasks.forEach(Runnable::run),
            new DelegatingPollListener(delegatePollListeners),
            new DelegatingPublicationListener(delegatePublicationListeners),
            new DelegatingTopicCreationListener(delegateTopicCreationListeners));
    }
}
