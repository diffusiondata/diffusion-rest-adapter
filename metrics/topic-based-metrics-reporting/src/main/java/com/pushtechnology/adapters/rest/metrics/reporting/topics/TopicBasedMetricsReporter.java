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

package com.pushtechnology.adapters.rest.metrics.reporting.topics;

import static com.pushtechnology.diffusion.client.topics.details.TopicType.DOUBLE;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.INT64;
import static java.math.RoundingMode.HALF_UP;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.OptionalLong;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metric.reporters.PollEventQuerier;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.RemovalCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater.UpdateCallback;
import com.pushtechnology.diffusion.client.session.Session;

import net.jcip.annotations.GuardedBy;

/**
 * A topic based metrics reporter.
 *
 * @author Matt Champion 27/06/2017
 */
public final class TopicBasedMetricsReporter implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(TopicBasedMetricsReporter.class);
    private static final NumberFormat FORMAT;
    private final Session session;
    private final ScheduledExecutorService executor;
    private final PollEventQuerier pollEventQuerier;
    private final String rootTopic;
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
    public TopicBasedMetricsReporter(
        Session session,
        ScheduledExecutorService executor,
        PollEventQuerier pollEventQuerier,
        String rootTopic) {

        this.session = session;
        this.executor = executor;
        this.pollEventQuerier = pollEventQuerier;
        this.rootTopic = rootTopic;
    }

    /**
     * Start logging the metrics.
     */
    @PostConstruct
    public synchronized void start() {
        final TopicControl topicControl = session.feature(TopicControl.class);

        allOf(
            topicControl.addTopic(rootTopic + "/poll/failureThroughput", DOUBLE),
            topicControl.addTopic(rootTopic + "/poll/requestThroughput", DOUBLE),
            topicControl.addTopic(rootTopic + "/poll/maximumSuccessfulRequestTime", INT64),
            topicControl.addTopic(rootTopic + "/poll/minimumSuccessfulRequestTime", INT64),
            topicControl.addTopic(rootTopic + "/poll/successfulRequestTimeNinetiethPercentile", INT64)
        )
            .thenRun(this::beginReporting)
            .exceptionally(e -> {
                LOG.warn("Failed to create metrics topics", e);
                close();
                return null;
            });
    }

    @PreDestroy
    @Override
    public synchronized void close() {
        session.feature(TopicControl.class).remove("?" + rootTopic + "/", new RemovalCallback() {
            @Override
            public void onTopicsRemoved() {
            }

            @Override
            public void onError(ErrorReason errorReason) {
                LOG.warn("Failed to remove metrics reporting topics: {}", errorReason);
            }
        });

        if (loggingTask != null) {
            loggingTask.cancel(false);
            loggingTask = null;
        }
    }

    private void beginReporting() {
        if (loggingTask != null) {
            return;
        }

        final TopicUpdateControl updateControl = session.feature(TopicUpdateControl.class);

        loggingTask = executor.scheduleAtFixedRate(
            () -> {
                reportPollEvents(updateControl);
            },
            1,
            1,
            MINUTES);
    }

    private void reportPollEvents(TopicUpdateControl updateControl) {
        final UpdateCallback updateCallback = new UpdateCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(ErrorReason errorReason) {
                LOG.warn("Failed to update metrics reporting topics: {}", errorReason);
            }
        };

        final TopicUpdateControl.ValueUpdater<Double> doubleUpdater = updateControl
            .updater()
            .valueUpdater(Double.class);
        final TopicUpdateControl.ValueUpdater<Long> longUpdater = updateControl
            .updater()
            .valueUpdater(Long.class);

        final OptionalLong requestTime = pollEventQuerier.get90thPercentileSuccessfulRequestTime();
        final BigDecimal failureThroughput = pollEventQuerier.getFailureThroughput();
        final BigDecimal requestThroughput = pollEventQuerier.getRequestThroughput();

        doubleUpdater.update(rootTopic + "/poll/failureThroughput", failureThroughput.doubleValue(), updateCallback);
        doubleUpdater.update(rootTopic + "/poll/requestThroughput", requestThroughput.doubleValue(), updateCallback);

        final OptionalLong maximumSuccessfulRequestTime = pollEventQuerier.getMaximumSuccessfulRequestTime();
        final OptionalLong minimumSuccessfulRequestTime = pollEventQuerier.getMinimumSuccessfulRequestTime();
        if (requestTime.isPresent()) {
            longUpdater.update(
                rootTopic + "/poll/successfulRequestTimeNinetiethPercentile",
                requestTime.getAsLong(),
                updateCallback);
        }
        if (maximumSuccessfulRequestTime.isPresent()) {
            longUpdater.update(
                rootTopic + "/poll/maximumSuccessfulRequestTime",
                maximumSuccessfulRequestTime.getAsLong(),
                updateCallback);
        }
        if (minimumSuccessfulRequestTime.isPresent()) {
            longUpdater.update(
                rootTopic + "/poll/minimumSuccessfulRequestTime",
                minimumSuccessfulRequestTime.getAsLong(),
                updateCallback);
        }
    }
}
