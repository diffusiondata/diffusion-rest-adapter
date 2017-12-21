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
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPollEventCollector;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPublicationEventCollector;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedTopicCreationEventCollector;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.publication.UpdateContext;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.diffusion.client.callbacks.Registration;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.RemovalCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicType;

/**
 * Unit tests for {@link TopicBasedMetricsReporter}.
 *
 * @author Push Technology Limited
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
    @Mock
    private Registration registration;
    @Mock
    private TopicManagementClient topicManagementClient;
    @Mock
    private PublishingClient publishingClient;
    @Mock
    private UpdateContext updateContext;

    private TopicBasedMetricsReporter reporter;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        initMocks(this);

        when(executor.scheduleAtFixedRate(isA(Runnable.class), isA(Long.class), isA(Long.class), isA(TimeUnit.class)))
            .thenReturn(loggingTask);

        when(session.feature(TopicControl.class)).thenReturn(topicControl);
        when(session.feature(TopicUpdateControl.class)).thenReturn(updateControl);

        when(topicManagementClient.addTopic(isNotNull(), ArgumentMatchers.<TopicType>isNotNull())).thenReturn(completedFuture(null));

        when(topicControl.removeTopicsWithSession("metrics")).thenReturn(completedFuture(registration));

        when(publishingClient.createUpdateContext(anyString(), isNotNull())).thenReturn(updateContext);

        reporter = new TopicBasedMetricsReporter(
            session,
            topicManagementClient,
            publishingClient,
            new PollEventCounter(),
            new PublicationEventCounter(),
            new TopicCreationEventCounter(),
            executor,
            new PollEventQuerier(new BoundedPollEventCollector(100)),
            new PublicationEventQuerier(new BoundedPublicationEventCollector(100)),
            new TopicCreationEventQuerier(new BoundedTopicCreationEventCollector(100)),
            "metrics");
    }

    @After
    public void postConditions() {
        Mockito.verifyNoMoreInteractions(executor, loggingTask, topicControl, updateControl, topicManagementClient);
    }

    @Test
    public void start() throws Exception {
        reporter.start();

        verify(topicControl).removeTopicsWithSession("metrics");
        verify(topicManagementClient).addTopic("metrics/poll/requests", INT64);
        verify(topicManagementClient).addTopic("metrics/poll/successes", INT64);
        verify(topicManagementClient).addTopic("metrics/poll/failures", INT64);
        verify(topicManagementClient).addTopic("metrics/poll/bytes", INT64);
        verify(topicManagementClient).addTopic("metrics/poll/failureThroughput", DOUBLE);
        verify(topicManagementClient).addTopic("metrics/poll/requestThroughput", DOUBLE);
        verify(topicManagementClient).addTopic("metrics/poll/maximumSuccessfulRequestTime", INT64);
        verify(topicManagementClient).addTopic("metrics/poll/minimumSuccessfulRequestTime", INT64);
        verify(topicManagementClient).addTopic("metrics/poll/successfulRequestTimeNinetiethPercentile", INT64);
        verify(topicManagementClient).addTopic("metrics/publication/requests", INT64);
        verify(topicManagementClient).addTopic("metrics/publication/successes", INT64);
        verify(topicManagementClient).addTopic("metrics/publication/failures", INT64);
        verify(topicManagementClient).addTopic("metrics/publication/bytes", INT64);
        verify(topicManagementClient).addTopic("metrics/publication/failureThroughput", DOUBLE);
        verify(topicManagementClient).addTopic("metrics/publication/requestThroughput", DOUBLE);
        verify(topicManagementClient).addTopic("metrics/publication/meanBytesPerPublication", DOUBLE);
        verify(topicManagementClient).addTopic("metrics/publication/maximumSuccessfulRequestTime", INT64);
        verify(topicManagementClient).addTopic("metrics/publication/minimumSuccessfulRequestTime", INT64);
        verify(topicManagementClient).addTopic("metrics/publication/successfulRequestTimeNinetiethPercentile", INT64);
        verify(topicManagementClient).addTopic("metrics/topicCreation/requests", INT64);
        verify(topicManagementClient).addTopic("metrics/topicCreation/successes", INT64);
        verify(topicManagementClient).addTopic("metrics/topicCreation/failures", INT64);
        verify(topicManagementClient).addTopic("metrics/topicCreation/failureThroughput", DOUBLE);
        verify(topicManagementClient).addTopic("metrics/topicCreation/requestThroughput", DOUBLE);
        verify(topicManagementClient).addTopic("metrics/topicCreation/maximumSuccessfulRequestTime", INT64);
        verify(topicManagementClient).addTopic("metrics/topicCreation/minimumSuccessfulRequestTime", INT64);
        verify(topicManagementClient).addTopic("metrics/topicCreation/successfulRequestTimeNinetiethPercentile", INT64);

        verify(executor).scheduleAtFixedRate(isA(Runnable.class), eq(1L), eq(1L), eq(MINUTES));
    }

    @Test
    public void startTwice() throws Exception {
        start();

        reporter.start();

        verify(topicControl, times(2)).removeTopicsWithSession("metrics");
        verify(topicManagementClient, times(2)).addTopic("metrics/poll/requests", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/poll/successes", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/poll/failures", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/poll/bytes", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/poll/failureThroughput", DOUBLE);
        verify(topicManagementClient, times(2)).addTopic("metrics/poll/requestThroughput", DOUBLE);
        verify(topicManagementClient, times(2)).addTopic("metrics/poll/maximumSuccessfulRequestTime", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/poll/minimumSuccessfulRequestTime", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/poll/successfulRequestTimeNinetiethPercentile", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/publication/requests", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/publication/successes", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/publication/failures", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/publication/bytes", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/publication/failureThroughput", DOUBLE);
        verify(topicManagementClient, times(2)).addTopic("metrics/publication/requestThroughput", DOUBLE);
        verify(topicManagementClient, times(2)).addTopic("metrics/publication/meanBytesPerPublication", DOUBLE);
        verify(topicManagementClient, times(2)).addTopic("metrics/publication/maximumSuccessfulRequestTime", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/publication/minimumSuccessfulRequestTime", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/publication/successfulRequestTimeNinetiethPercentile", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/topicCreation/requests", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/topicCreation/successes", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/topicCreation/failures", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/topicCreation/failureThroughput", DOUBLE);
        verify(topicManagementClient, times(2)).addTopic("metrics/topicCreation/requestThroughput", DOUBLE);
        verify(topicManagementClient, times(2)).addTopic("metrics/topicCreation/maximumSuccessfulRequestTime", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/topicCreation/minimumSuccessfulRequestTime", INT64);
        verify(topicManagementClient, times(2)).addTopic("metrics/topicCreation/successfulRequestTimeNinetiethPercentile", INT64);
        verify(executor).scheduleAtFixedRate(isA(Runnable.class), eq(1L), eq(1L), eq(MINUTES));
    }

    @Test
    public void close() throws Exception {
        start();

        reporter.close();

        verify(registration).close();
        verify(topicControl).remove(eq("?metrics/"), isA(RemovalCallback.class));
        verify(loggingTask).cancel(false);
    }

    @Test
    public void closeBeforeStart() throws Exception {
        reporter.close();

        verify(topicControl).remove(eq("?metrics/"), isA(RemovalCallback.class));
    }
}