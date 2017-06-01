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

package com.pushtechnology.adapters.rest.metric.reporters;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.annotation.GuardedBy;
import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metrics.listeners.PollEventCounter;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationEventCounter;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationEventCounter;

/**
 * Simple metrics collector that counts all events and logs a summary.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class EventCountReporter implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EventCountReporter.class);

    private final PollEventCounter pollEventCounter = new PollEventCounter();
    private final PublicationEventCounter publicationEventCounter = new PublicationEventCounter();
    private final TopicCreationEventCounter topicCreationEventCounter = new TopicCreationEventCounter();

    private final ScheduledExecutorService executor;
    @GuardedBy("this")
    private Future<?> loggingTask;

    /**
     * Constructor.
     */
    public EventCountReporter(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Start logging the metrics.
     */
    @PostConstruct
    public synchronized void start() {
        if (loggingTask != null) {
            return;
        }

        loggingTask = executor.scheduleAtFixedRate(
            () -> LOG.info(
                "Polls {}/{}/{} Topic creation {}/{}/{} Updates {}/{}/{}",
                pollEventCounter.getRequests(),
                pollEventCounter.getSuccesses(),
                pollEventCounter.getFailures(),
                topicCreationEventCounter.getRequests(),
                topicCreationEventCounter.getSuccesses(),
                topicCreationEventCounter.getFailures(),
                publicationEventCounter.getRequests(),
                publicationEventCounter.getSuccesses(),
                publicationEventCounter.getFailures()),
            1,
            1,
            MINUTES);
    }

    @PreDestroy
    @Override
    public synchronized void close() {
        if (loggingTask != null) {
            loggingTask.cancel(false);
            loggingTask = null;
        }
    }
}
