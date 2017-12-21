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

import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.callbacks.Registration;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.RemovalCallback;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.DataType;

import net.jcip.annotations.GuardedBy;

/**
 * A topic based metrics reporter.
 *
 * @author Push Technology Limited
 */
public final class TopicBasedMetricsReporter implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(TopicBasedMetricsReporter.class);
    private static final NumberFormat FORMAT;
    private final Session session;
    private final TopicManagementClient topicManagementClient;
    private final PublishingClient publishingClient;
    private final PollEventCounter pollEventCounter;
    private final PublicationEventCounter publicationEventCounter;
    private final TopicCreationEventCounter topicCreationEventCounter;
    private final ScheduledExecutorService executor;
    private final PollEventQuerier pollEventQuerier;
    private final PublicationEventQuerier publicationQuerier;
    private final TopicCreationEventQuerier topicCreationQuerier;
    private final String rootTopic;
    @GuardedBy("this")
    private Future<?> loggingTask;
    @GuardedBy("this")
    private Registration registration;

    static {
        FORMAT = NumberFormat.getInstance();
        FORMAT.setMaximumFractionDigits(3);
        FORMAT.setMinimumFractionDigits(3);
        FORMAT.setRoundingMode(HALF_UP);
    }


    /**
     * Constructor.
     */
    // CHECKSTYLE.OFF: ParameterNumber
    public TopicBasedMetricsReporter(
        Session session,
        TopicManagementClient topicManagementClient,
        PublishingClient publishingClient,
        PollEventCounter pollEventCounter,
        PublicationEventCounter publicationEventCounter,
        TopicCreationEventCounter topicCreationEventCounter,
        ScheduledExecutorService executor,
        PollEventQuerier pollEventQuerier,
        PublicationEventQuerier publicationQuerier,
        TopicCreationEventQuerier topicCreationQuerier,
        String rootTopic) {
    // CHECKSTYLE.ON: ParameterNumber

        this.session = session;
        this.topicManagementClient = topicManagementClient;
        this.publishingClient = publishingClient;
        this.pollEventCounter = pollEventCounter;
        this.publicationEventCounter = publicationEventCounter;
        this.topicCreationEventCounter = topicCreationEventCounter;
        this.executor = executor;
        this.pollEventQuerier = pollEventQuerier;
        this.publicationQuerier = publicationQuerier;
        this.topicCreationQuerier = topicCreationQuerier;
        this.rootTopic = rootTopic;
    }

    /**
     * Start logging the metrics.
     */
    @PostConstruct
    public synchronized void start() {
        final TopicControl topicControl = session.feature(TopicControl.class);

        allOf(
            topicControl.removeTopicsWithSession(rootTopic)
                .thenAccept(registration -> {
                    synchronized (this) {
                        this.registration = registration;
                    }
                }),
            topicManagementClient.addTopic(rootTopic + "/poll/requests", INT64),
            topicManagementClient.addTopic(rootTopic + "/poll/successes", INT64),
            topicManagementClient.addTopic(rootTopic + "/poll/failures", INT64),
            topicManagementClient.addTopic(rootTopic + "/poll/bytes", INT64),
            topicManagementClient.addTopic(rootTopic + "/poll/failureThroughput", DOUBLE),
            topicManagementClient.addTopic(rootTopic + "/poll/requestThroughput", DOUBLE),
            topicManagementClient.addTopic(rootTopic + "/poll/maximumSuccessfulRequestTime", INT64),
            topicManagementClient.addTopic(rootTopic + "/poll/minimumSuccessfulRequestTime", INT64),
            topicManagementClient.addTopic(rootTopic + "/poll/successfulRequestTimeNinetiethPercentile", INT64),
            topicManagementClient.addTopic(rootTopic + "/publication/requests", INT64),
            topicManagementClient.addTopic(rootTopic + "/publication/successes", INT64),
            topicManagementClient.addTopic(rootTopic + "/publication/failures", INT64),
            topicManagementClient.addTopic(rootTopic + "/publication/bytes", INT64),
            topicManagementClient.addTopic(rootTopic + "/publication/failureThroughput", DOUBLE),
            topicManagementClient.addTopic(rootTopic + "/publication/requestThroughput", DOUBLE),
            topicManagementClient.addTopic(rootTopic + "/publication/meanBytesPerPublication", DOUBLE),
            topicManagementClient.addTopic(rootTopic + "/publication/maximumSuccessfulRequestTime", INT64),
            topicManagementClient.addTopic(rootTopic + "/publication/minimumSuccessfulRequestTime", INT64),
            topicManagementClient.addTopic(rootTopic + "/publication/successfulRequestTimeNinetiethPercentile", INT64),
            topicManagementClient.addTopic(rootTopic + "/topicCreation/requests", INT64),
            topicManagementClient.addTopic(rootTopic + "/topicCreation/successes", INT64),
            topicManagementClient.addTopic(rootTopic + "/topicCreation/failures", INT64),
            topicManagementClient.addTopic(rootTopic + "/topicCreation/failureThroughput", DOUBLE),
            topicManagementClient.addTopic(rootTopic + "/topicCreation/requestThroughput", DOUBLE),
            topicManagementClient.addTopic(rootTopic + "/topicCreation/maximumSuccessfulRequestTime", INT64),
            topicManagementClient.addTopic(rootTopic + "/topicCreation/minimumSuccessfulRequestTime", INT64),
            topicManagementClient
                .addTopic(rootTopic + "/topicCreation/successfulRequestTimeNinetiethPercentile", INT64))
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
        if (registration != null) {
            registration.close();
        }
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

    private void reportPollEvents() {
        final DataType<Long> int64 = Diffusion.dataTypes().int64();
        publishingClient
            .createUpdateContext(rootTopic + "/poll/requests", int64)
            .publish((long) pollEventCounter.getRequests());
        publishingClient
            .createUpdateContext(rootTopic + "/poll/successes", int64)
            .publish((long) pollEventCounter.getSuccesses());
        publishingClient
            .createUpdateContext(rootTopic + "/poll/failures", int64)
            .publish((long) pollEventCounter.getFailures());
        publishingClient
            .createUpdateContext(rootTopic + "/poll/bytes", int64)
            .publish(pollEventCounter.getTotalPollResponseBytes());

        final BigDecimal failureThroughput = pollEventQuerier.getFailureThroughput();
        final BigDecimal requestThroughput = pollEventQuerier.getRequestThroughput();

        final DataType<Double> doubleFloat = Diffusion.dataTypes().doubleFloat();
        publishingClient
            .createUpdateContext(rootTopic + "/poll/failureThroughput", doubleFloat)
            .publish(failureThroughput.doubleValue());
        publishingClient
            .createUpdateContext(rootTopic + "/poll/requestThroughput", doubleFloat)
            .publish(requestThroughput.doubleValue());

        final OptionalLong requestTime = pollEventQuerier.get90thPercentileSuccessfulRequestTime();
        final OptionalLong maximumSuccessfulRequestTime = pollEventQuerier.getMaximumSuccessfulRequestTime();
        final OptionalLong minimumSuccessfulRequestTime = pollEventQuerier.getMinimumSuccessfulRequestTime();
        if (requestTime.isPresent()) {
            publishingClient
                .createUpdateContext(rootTopic + "/poll/successfulRequestTimeNinetiethPercentile", int64)
                .publish(requestTime.getAsLong());
        }
        if (maximumSuccessfulRequestTime.isPresent()) {
            publishingClient
                .createUpdateContext(rootTopic + "/poll/maximumSuccessfulRequestTime", int64)
                .publish(maximumSuccessfulRequestTime.getAsLong());
        }
        if (minimumSuccessfulRequestTime.isPresent()) {
            publishingClient
                .createUpdateContext(rootTopic + "/poll/minimumSuccessfulRequestTime", int64)
                .publish(minimumSuccessfulRequestTime.getAsLong());
        }
    }

    private void reportPublicationEvents() {
        final DataType<Long> int64 = Diffusion.dataTypes().int64();
        publishingClient
            .createUpdateContext(rootTopic + "/publication/requests", int64)
            .publish((long) publicationEventCounter.getRequests());
        publishingClient
            .createUpdateContext(rootTopic + "/publication/successes", int64)
            .publish((long) publicationEventCounter.getSuccesses());
        publishingClient
            .createUpdateContext(rootTopic + "/publication/failures", int64)
            .publish((long) publicationEventCounter.getFailures());
        publishingClient
            .createUpdateContext(rootTopic + "/publication/bytes", int64)
            .publish((long) publicationEventCounter.getTotalSuccessBytes());

        final BigDecimal failureThroughput = publicationQuerier.getFailureThroughput();
        final BigDecimal requestThroughput = publicationQuerier.getRequestThroughput();

        final DataType<Double> doubleFloat = Diffusion.dataTypes().doubleFloat();
        publishingClient
            .createUpdateContext(rootTopic + "/publication/failureThroughput", doubleFloat)
            .publish(failureThroughput.doubleValue());
        publishingClient
            .createUpdateContext(rootTopic + "/publication/requestThroughput", doubleFloat)
            .publish(requestThroughput.doubleValue());
        publishingClient
            .createUpdateContext(rootTopic + "/publication/meanBytesPerPublication", doubleFloat)
            .publish(publicationQuerier.getMeanPublicationSize().doubleValue());

        final OptionalLong requestTime = publicationQuerier.get90thPercentileSuccessfulRequestTime();
        final OptionalLong maximumSuccessfulRequestTime = publicationQuerier.getMaximumSuccessfulRequestTime();
        final OptionalLong minimumSuccessfulRequestTime = publicationQuerier.getMinimumSuccessfulRequestTime();
        if (requestTime.isPresent()) {
            publishingClient
                .createUpdateContext(rootTopic + "/publication/successfulRequestTimeNinetiethPercentile", int64)
                .publish(requestTime.getAsLong());
        }
        if (maximumSuccessfulRequestTime.isPresent()) {
            publishingClient
                .createUpdateContext(rootTopic + "/publication/maximumSuccessfulRequestTime", int64)
                .publish(maximumSuccessfulRequestTime.getAsLong());
        }
        if (minimumSuccessfulRequestTime.isPresent()) {
            publishingClient
                .createUpdateContext(rootTopic + "/publication/minimumSuccessfulRequestTime", int64)
                .publish(minimumSuccessfulRequestTime.getAsLong());
        }
    }

    private void reportTopicCreationEvents() {
        final DataType<Long> int64 = Diffusion.dataTypes().int64();
        publishingClient
            .createUpdateContext(rootTopic + "/topicCreation/requests", int64)
            .publish((long) topicCreationEventCounter.getRequests());
        publishingClient
            .createUpdateContext(rootTopic + "/topicCreation/successes", int64)
            .publish((long) topicCreationEventCounter.getSuccesses());
        publishingClient
            .createUpdateContext(rootTopic + "/topicCreation/failures", int64)
            .publish((long) topicCreationEventCounter.getFailures());

        final BigDecimal failureThroughput = topicCreationQuerier.getFailureThroughput();
        final BigDecimal requestThroughput = topicCreationQuerier.getRequestThroughput();

        final DataType<Double> doubleFloat = Diffusion.dataTypes().doubleFloat();
        publishingClient
            .createUpdateContext(rootTopic + "/topicCreation/failureThroughput", doubleFloat)
            .publish(failureThroughput.doubleValue());
        publishingClient
            .createUpdateContext(rootTopic + "/topicCreation/requestThroughput", doubleFloat)
            .publish(requestThroughput.doubleValue());

        final OptionalLong requestTime = topicCreationQuerier.get90thPercentileSuccessfulRequestTime();
        final OptionalLong maximumSuccessfulRequestTime = topicCreationQuerier.getMaximumSuccessfulRequestTime();
        final OptionalLong minimumSuccessfulRequestTime = topicCreationQuerier.getMinimumSuccessfulRequestTime();
        if (requestTime.isPresent()) {
            publishingClient
                .createUpdateContext(rootTopic + "/topicCreation/successfulRequestTimeNinetiethPercentile", int64)
                .publish(requestTime.getAsLong());
        }
        if (maximumSuccessfulRequestTime.isPresent()) {
            publishingClient
                .createUpdateContext(rootTopic + "/topicCreation/maximumSuccessfulRequestTime", int64)
                .publish(maximumSuccessfulRequestTime.getAsLong());
        }
        if (minimumSuccessfulRequestTime.isPresent()) {
            publishingClient
                .createUpdateContext(rootTopic + "/topicCreation/minimumSuccessfulRequestTime", int64)
                .publish(minimumSuccessfulRequestTime.getAsLong());
        }
    }
}
