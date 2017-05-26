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

import static java.math.RoundingMode.HALF_UP;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.OptionalLong;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.annotation.GuardedBy;
import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event reporter.
 *
 * @author Matt Champion 14/05/2017
 */
@ThreadSafe
public final class EventReporter implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EventReporter.class);
    private static final NumberFormat FORMAT;
    private final ScheduledExecutorService executor;
    private final PollEventQuerier pollEventQuerier;
    private final PublicationEventQuerier publicationEventQuerier;
    private final TopicCreationEventQuerier topicCreationEventQuerier;
    @GuardedBy("this")
    private Future<?> loggingTask;

    static {
        FORMAT = NumberFormat.getInstance();
        FORMAT.setMaximumFractionDigits(3);
        FORMAT.setMinimumFractionDigits(3);
        FORMAT.setRoundingMode(HALF_UP);
    }

    /**
     * Constructor.
     */
    public EventReporter(
            ScheduledExecutorService executor,
            PollEventQuerier pollEventQuerier,
            PublicationEventQuerier publicationEventQuerier,
            TopicCreationEventQuerier topicCreationEventQuerier) {
        this.executor = executor;
        this.pollEventQuerier = pollEventQuerier;
        this.publicationEventQuerier = publicationEventQuerier;
        this.topicCreationEventQuerier = topicCreationEventQuerier;
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
            () -> {
                reportPollEvents();
                reportPublicationEvents();
                reportTopicCreationEvents();
            },
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

    private void reportPollEvents() {
        final BigDecimal requestThroughput = pollEventQuerier.getPollRequestThroughput();
        final OptionalLong minimumSuccessfulRequestTime = pollEventQuerier.getMinimumSuccessfulRequestTime();
        final OptionalLong maximumSuccessfulRequestTime = pollEventQuerier.getMaximumSuccessfulRequestTime();
        final BigDecimal pollFailureThroughput = pollEventQuerier.getPollFailureThroughput();
        LOG.info(
            "Poll request throughput: {} /s",
            FORMAT.format(requestThroughput));
        LOG.info(
            "Min poll time: {} ms",
            minimumSuccessfulRequestTime.orElse(-1));
        LOG.info(
            "Max poll time: {} ms",
            maximumSuccessfulRequestTime.orElse(-1));
        LOG.info(
            "Poll failure throughput {} /s",
            FORMAT.format(pollFailureThroughput));
    }

    private void reportPublicationEvents() {
        final BigDecimal requestThroughput = publicationEventQuerier.getPublicationRequestThroughput();
        final OptionalLong minimumSuccessfulRequestTime = publicationEventQuerier.getMinimumSuccessfulRequestTime();
        final OptionalLong maximumSuccessfulRequestTime = publicationEventQuerier.getMaximumSuccessfulRequestTime();
        final BigDecimal pollFailureThroughput = publicationEventQuerier.getPublicationFailureThroughput();
        LOG.info(
            "Publication request throughput: {} /s",
            FORMAT.format(requestThroughput));
        LOG.info(
            "Min publication time: {} ms",
            minimumSuccessfulRequestTime.orElse(-1));
        LOG.info(
            "Max publication time: {} ms",
            maximumSuccessfulRequestTime.orElse(-1));
        LOG.info(
            "Publication failure throughput {} /s",
            FORMAT.format(pollFailureThroughput));
    }

    private void reportTopicCreationEvents() {
        final BigDecimal requestThroughput = topicCreationEventQuerier.getTopicCreationRequestThroughput();
        final OptionalLong minimumSuccessfulRequestTime = topicCreationEventQuerier.getMinimumSuccessfulRequestTime();
        final OptionalLong maximumSuccessfulRequestTime = topicCreationEventQuerier.getMaximumSuccessfulRequestTime();
        final BigDecimal pollFailureThroughput = topicCreationEventQuerier.getTopicCreationFailureThroughput();
        LOG.info(
            "Topic creation request throughput: {} /s",
            FORMAT.format(requestThroughput));
        LOG.info(
            "Min topic creation time: {} ms",
            minimumSuccessfulRequestTime.orElse(-1));
        LOG.info(
            "Max topic creation time: {} ms",
            maximumSuccessfulRequestTime.orElse(-1));
        LOG.info(
            "Topic creation failure throughput {} /s",
            FORMAT.format(pollFailureThroughput));
    }
}
