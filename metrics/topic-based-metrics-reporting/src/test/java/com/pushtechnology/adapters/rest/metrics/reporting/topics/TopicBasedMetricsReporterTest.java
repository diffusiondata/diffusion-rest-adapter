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

import static com.pushtechnology.diffusion.client.features.control.topics.TopicControl.AddTopicResult.CREATED;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.DOUBLE;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.INT64;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.pushtechnology.adapters.rest.metric.reporters.PollEventQuerier;
import com.pushtechnology.adapters.rest.metric.reporters.PublicationEventQuerier;
import com.pushtechnology.adapters.rest.metric.reporters.TopicCreationEventQuerier;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPollEventCollector;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPublicationEventCollector;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedTopicCreationEventCollector;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.RemovalCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicType;

/**
 * Unit tests for {@link TopicBasedMetricsReporter}.
 *
 * @author Matt Champion 27/06/2017
 */
public final class TopicBasedMetricsReporterTest {
    @Mock
    private ScheduledExecutorService executor;
    @Mock
    private ScheduledFuture loggingTask;
    @Mock
    private Session session;
    @Mock
    private TopicControl topicControl;
    @Mock
    private TopicUpdateControl updateControl;

    private TopicBasedMetricsReporter reporter;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        initMocks(this);

        when(executor.scheduleAtFixedRate(isA(Runnable.class), isA(Long.class), isA(Long.class), isA(TimeUnit.class)))
            .thenReturn(loggingTask);

        when(session.feature(TopicControl.class)).thenReturn(topicControl);
        when(session.feature(TopicUpdateControl.class)).thenReturn(updateControl);

        when(topicControl.addTopic(isNotNull(), ArgumentMatchers.<TopicType>isNotNull())).thenReturn(completedFuture(CREATED));

        reporter = new TopicBasedMetricsReporter(
            session,
            executor,
            new PollEventQuerier(new BoundedPollEventCollector(100)),
            new PublicationEventQuerier(new BoundedPublicationEventCollector(100)),
            new TopicCreationEventQuerier(new BoundedTopicCreationEventCollector(100)),
            "metrics");
    }

    @After
    public void postConditions() {
        Mockito.verifyNoMoreInteractions(executor, loggingTask, topicControl, updateControl);
    }

    @Test
    public void start() throws Exception {
        reporter.start();

        verify(topicControl).addTopic("metrics/poll/failureThroughput", DOUBLE);
        verify(topicControl).addTopic("metrics/poll/requestThroughput", DOUBLE);
        verify(topicControl).addTopic("metrics/poll/maximumSuccessfulRequestTime", INT64);
        verify(topicControl).addTopic("metrics/poll/minimumSuccessfulRequestTime", INT64);
        verify(topicControl).addTopic("metrics/poll/successfulRequestTimeNinetiethPercentile", INT64);
        verify(topicControl).addTopic("metrics/publication/failureThroughput", DOUBLE);
        verify(topicControl).addTopic("metrics/publication/requestThroughput", DOUBLE);
        verify(topicControl).addTopic("metrics/publication/maximumSuccessfulRequestTime", INT64);
        verify(topicControl).addTopic("metrics/publication/minimumSuccessfulRequestTime", INT64);
        verify(topicControl).addTopic("metrics/publication/successfulRequestTimeNinetiethPercentile", INT64);
        verify(topicControl).addTopic("metrics/topicCreation/failureThroughput", DOUBLE);
        verify(topicControl).addTopic("metrics/topicCreation/requestThroughput", DOUBLE);
        verify(topicControl).addTopic("metrics/topicCreation/maximumSuccessfulRequestTime", INT64);
        verify(topicControl).addTopic("metrics/topicCreation/minimumSuccessfulRequestTime", INT64);
        verify(topicControl).addTopic("metrics/topicCreation/successfulRequestTimeNinetiethPercentile", INT64);

        verify(executor).scheduleAtFixedRate(isA(Runnable.class), eq(1L), eq(1L), eq(MINUTES));
    }

    @Test
    public void startTwice() throws Exception {
        start();

        reporter.start();

        verify(topicControl, times(2)).addTopic("metrics/poll/failureThroughput", DOUBLE);
        verify(topicControl, times(2)).addTopic("metrics/poll/requestThroughput", DOUBLE);
        verify(topicControl, times(2)).addTopic("metrics/poll/maximumSuccessfulRequestTime", INT64);
        verify(topicControl, times(2)).addTopic("metrics/poll/minimumSuccessfulRequestTime", INT64);
        verify(topicControl, times(2)).addTopic("metrics/poll/successfulRequestTimeNinetiethPercentile", INT64);
        verify(topicControl, times(2)).addTopic("metrics/publication/failureThroughput", DOUBLE);
        verify(topicControl, times(2)).addTopic("metrics/publication/requestThroughput", DOUBLE);
        verify(topicControl, times(2)).addTopic("metrics/publication/maximumSuccessfulRequestTime", INT64);
        verify(topicControl, times(2)).addTopic("metrics/publication/minimumSuccessfulRequestTime", INT64);
        verify(topicControl, times(2)).addTopic("metrics/publication/successfulRequestTimeNinetiethPercentile", INT64);
        verify(topicControl, times(2)).addTopic("metrics/topicCreation/failureThroughput", DOUBLE);
        verify(topicControl, times(2)).addTopic("metrics/topicCreation/requestThroughput", DOUBLE);
        verify(topicControl, times(2)).addTopic("metrics/topicCreation/maximumSuccessfulRequestTime", INT64);
        verify(topicControl, times(2)).addTopic("metrics/topicCreation/minimumSuccessfulRequestTime", INT64);
        verify(topicControl, times(2)).addTopic("metrics/topicCreation/successfulRequestTimeNinetiethPercentile", INT64);
        verify(executor).scheduleAtFixedRate(isA(Runnable.class), eq(1L), eq(1L), eq(MINUTES));
    }

    @Test
    public void close() throws Exception {
        start();

        reporter.close();

        verify(topicControl).remove(eq("?metrics/"), isA(RemovalCallback.class));
        verify(loggingTask).cancel(false);
    }

    @Test
    public void closeBeforeStart() throws Exception {
        reporter.close();

        verify(topicControl).remove(eq("?metrics/"), isA(RemovalCallback.class));
    }
}