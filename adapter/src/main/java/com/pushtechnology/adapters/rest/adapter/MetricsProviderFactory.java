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

import static com.pushtechnology.adapters.rest.model.latest.MetricsConfig.Type.COUNTING;
import static com.pushtechnology.adapters.rest.model.latest.MetricsConfig.Type.SUMMARY;

import java.util.concurrent.ScheduledExecutorService;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.injectors.ProviderAdapter;

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
import com.pushtechnology.adapters.rest.metrics.listeners.PollEventCounter;
import com.pushtechnology.adapters.rest.metrics.listeners.PollListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationEventCounter;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationEventCounter;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;
import com.pushtechnology.adapters.rest.model.latest.MetricsConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;

/**
 * Factory for {@link MetricsProvider}.
 *
 * @author Push Technology Limited
 */
public final class MetricsProviderFactory extends ProviderAdapter {

    /**
     * @return a new metrics provider
     */
    public MetricsProvider provide(Model model, ScheduledExecutorService executorService) {
        final MutablePicoContainer factoryContainer = new PicoBuilder()
            .withCaching()
            .withConstructorInjection()
            .withJavaEE5Lifecycle()
            .withLocking()
            .build()
            .addComponent(executorService);

        final MetricsConfig.Type type = model.getMetrics().getType();
        final Runnable startTask;
        final Runnable stopTask;
        if (COUNTING.equals(type)) {
            factoryContainer
                .addComponent(PollEventCounter.class)
                .addComponent(PublicationEventCounter.class)
                .addComponent(TopicCreationEventCounter.class)
                .addComponent(EventCountReporter.class);
            startTask = factoryContainer.getComponent(EventCountReporter.class)::start;
            stopTask = factoryContainer.getComponent(EventCountReporter.class)::close;
        }
        else if (SUMMARY.equals(type)) {
            factoryContainer
                .addComponent(PollEventDispatcher.class)
                .addComponent(PublicationEventDispatcher.class)
                .addComponent(TopicCreationEventDispatcher.class)
                .addComponent(new BoundedPollEventCollector(100))
                .addComponent(new BoundedPublicationEventCollector(100))
                .addComponent(new BoundedTopicCreationEventCollector(100))
                .addComponent(PollEventQuerier.class)
                .addComponent(PublicationEventQuerier.class)
                .addComponent(TopicCreationEventQuerier.class)
                .addComponent(EventSummaryReporter.class);
            startTask = factoryContainer.getComponent(EventSummaryReporter.class)::start;
            stopTask = factoryContainer.getComponent(EventSummaryReporter.class)::close;
        }
        else {
            factoryContainer
                .addComponent(PollListener.NULL_LISTENER)
                .addComponent(PublicationListener.NULL_LISTENER)
                .addComponent(TopicCreationListener.NULL_LISTENER);
            startTask = () -> { };
            stopTask = () -> { };
        }

        return new MetricsProvider(
            startTask,
            stopTask,
            factoryContainer.getComponent(PollListener.class),
            factoryContainer.getComponent(PublicationListener.class),
            factoryContainer.getComponent(TopicCreationListener.class));
    }
}
