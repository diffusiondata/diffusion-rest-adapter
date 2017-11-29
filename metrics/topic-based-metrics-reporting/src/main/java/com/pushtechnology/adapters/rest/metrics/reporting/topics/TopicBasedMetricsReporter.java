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

import static com.pushtechnology.diffusion.client.Diffusion.dataTypes;
import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.EXISTS;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metric.reporters.PollEventQuerier;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.AddCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.RemovalCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater.UpdateCallback;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.json.JSON;

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
        createTopics(
            singletonList(rootTopic + "/poll"),
            this::close,
            this::beginReporting);
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

    private void createTopics(Collection<String> paths, Runnable onFailed, Runnable onSuccess) {
        final TopicControl topicControl = session.feature(TopicControl.class);

        final AddCallback addCallback = new AddCallback() {
            @GuardedBy("this")
            private final Set<String> topicsFailed = new HashSet<>();
            @GuardedBy("this")
            private final Set<String> topicsRemaining = new HashSet<>(paths);

            @Override
            public synchronized void onTopicAdded(String topicPath) {
                topicsRemaining.remove(topicPath);
                onResult();

            }

            @Override
            public synchronized void onTopicAddFailed(String topicPath, TopicAddFailReason reason) {
                topicsRemaining.remove(topicPath);
                if (!EXISTS.equals(reason)) {
                    topicsFailed.add(topicPath);
                }
                onResult();
            }

            @Override
            public synchronized void onDiscard() {
                onFailed.run();
            }

            @GuardedBy("this")
            private void onResult() {
                if (topicsRemaining.size() == 0) {
                    if (topicsFailed.size() == 0) {
                        onSuccess.run();
                    }
                    else {
                        onFailed.run();
                    }
                }
            }
        };

        paths.forEach(path -> topicControl.addTopic(path, TopicType.JSON, addCallback));
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
        final OptionalLong requestTime = pollEventQuerier.get90thPercentileSuccessfulRequestTime();
        final BigDecimal failureThroughput = pollEventQuerier.getFailureThroughput();
        final BigDecimal requestThroughput = pollEventQuerier.getRequestThroughput();
        final OptionalLong maximumSuccessfulRequestTime = pollEventQuerier.getMaximumSuccessfulRequestTime();
        final OptionalLong minimumSuccessfulRequestTime = pollEventQuerier.getMinimumSuccessfulRequestTime();
        String value = "{";
        if (requestTime.isPresent()) {
            final long successfulRequestTime = requestTime.getAsLong();
            value += "\"successfulRequestTimeNinetiethPercentile\":" + successfulRequestTime + ",";
        }
        else {
            value += "\"successfulRequestTimeNinetiethPercentile\":null,";
        }
        value += "\"failureThroughput\":" + failureThroughput + ",";
        value += "\"requestThroughput\":" + requestThroughput + ",";
        if (maximumSuccessfulRequestTime.isPresent()) {
            final long maximumRequestTime = maximumSuccessfulRequestTime.getAsLong();
            value += "\"maximumSuccessfulRequestTime\":" + maximumRequestTime + ",";
        }
        else {
            value += "\"maximumSuccessfulRequestTime\":null,";
        }
        if (minimumSuccessfulRequestTime.isPresent()) {
            final long minimumRequestTime = minimumSuccessfulRequestTime.getAsLong();
            value += "\"minimumSuccessfulRequestTime\":" + minimumRequestTime;
        }
        else {
            value += "\"minimumSuccessfulRequestTime\":null";
        }
        value += "}";

        updateControl
            .updater()
            .valueUpdater(JSON.class)
            .update(
                rootTopic + "/poll",
                dataTypes()
                    .json()
                    .fromJsonString(value),
                new UpdateCallback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError(ErrorReason errorReason) {
                    LOG.warn("Failed to update metrics reporting topics: {}", errorReason);
                }
            });
    }
}
