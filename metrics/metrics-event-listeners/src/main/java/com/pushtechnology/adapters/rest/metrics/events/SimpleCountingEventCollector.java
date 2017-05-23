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

package com.pushtechnology.adapters.rest.metrics.events;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.annotation.GuardedBy;
import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationSuccessEvent;

/**
 * Simple metrics collector that counts all events and logs a summary.
 *
 * @author Matt Champion 14/05/2017
 */
@ThreadSafe
public final class SimpleCountingEventCollector implements
        PollEventListener,
        PublicationEventListener,
        TopicCreationEventListener,
        AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleCountingEventCollector.class);

    private final AtomicInteger pollRequests = new AtomicInteger(0);
    private final AtomicInteger topicCreationRequests = new AtomicInteger(0);
    private final AtomicInteger publicationRequests = new AtomicInteger(0);
    private final AtomicInteger pollFailures = new AtomicInteger(0);
    private final AtomicInteger topicCreationFailures = new AtomicInteger(0);
    private final AtomicInteger publicationFailures = new AtomicInteger(0);
    private final AtomicInteger pollSuccesses = new AtomicInteger(0);
    private final AtomicInteger topicCreationSuccesses = new AtomicInteger(0);
    private final AtomicInteger publicationSuccesses = new AtomicInteger(0);

    private final ScheduledExecutorService executor;
    @GuardedBy("this")
    private Future<?> loggingTask;

    /**
     * Constructor.
     */
    public SimpleCountingEventCollector(ScheduledExecutorService executor) {
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
                getPollRequests(),
                getPollSuccesses(),
                getPollFailures(),
                getTopicCreationRequests(),
                getTopicCreationSuccesses(),
                getTopicCreationFailures(),
                getPublicationRequests(),
                getPublicationSuccesses(),
                getPublicationFailures()),
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

    /*package*/ int getPublicationFailures() {
        return publicationFailures.get();
    }

    /*package*/ int getPublicationSuccesses() {
        return publicationSuccesses.get();
    }

    /*package*/ int getPublicationRequests() {
        return publicationRequests.get();
    }

    /*package*/ int getTopicCreationFailures() {
        return topicCreationFailures.get();
    }

    /*package*/ int getTopicCreationSuccesses() {
        return topicCreationSuccesses.get();
    }

    /*package*/ int getTopicCreationRequests() {
        return topicCreationRequests.get();
    }

    /*package*/ int getPollFailures() {
        return pollFailures.get();
    }

    /*package*/ int getPollSuccesses() {
        return pollSuccesses.get();
    }

    /*package*/ int getPollRequests() {
        return pollRequests.get();
    }

    @Override
    public void onPollRequest(PollRequestEvent event) {
        pollRequests.incrementAndGet();
    }

    @Override
    public void onPollSuccess(PollSuccessEvent event) {
        pollSuccesses.incrementAndGet();
    }

    @Override
    public void onPollFailed(PollFailedEvent event) {
        pollFailures.incrementAndGet();
    }

    @Override
    public void onPublicationRequest(PublicationRequestEvent event) {
        publicationRequests.incrementAndGet();
    }

    @Override
    public void onPublicationSuccess(PublicationSuccessEvent event) {
        publicationSuccesses.incrementAndGet();
    }

    @Override
    public void onPublicationFailed(PublicationFailedEvent event) {
        publicationFailures.incrementAndGet();
    }

    @Override
    public void onTopicCreationRequest(TopicCreationRequestEvent event) {
        topicCreationRequests.incrementAndGet();
    }

    @Override
    public void onTopicCreationSuccess(TopicCreationSuccessEvent event) {
        topicCreationSuccesses.incrementAndGet();
    }

    @Override
    public void onTopicCreationFailed(TopicCreationFailedEvent event) {
        topicCreationFailures.incrementAndGet();
    }
}
