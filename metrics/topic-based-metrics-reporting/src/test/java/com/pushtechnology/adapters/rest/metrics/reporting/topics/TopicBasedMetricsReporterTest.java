package com.pushtechnology.adapters.rest.metrics.reporting.topics;

import static com.pushtechnology.diffusion.client.topics.details.TopicType.JSON;
import static java.util.concurrent.TimeUnit.MINUTES;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.pushtechnology.adapters.rest.metric.reporters.PollEventQuerier;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPollEventCollector;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.AddCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.RemovalCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.session.Session;

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

    @Captor
    private ArgumentCaptor<AddCallback> addCallbackCaptor;

    private TopicBasedMetricsReporter reporter;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        initMocks(this);

        when(executor.scheduleAtFixedRate(isA(Runnable.class), isA(Long.class), isA(Long.class), isA(TimeUnit.class)))
            .thenReturn(loggingTask);

        when(session.feature(TopicControl.class)).thenReturn(topicControl);
        when(session.feature(TopicUpdateControl.class)).thenReturn(updateControl);

        reporter = new TopicBasedMetricsReporter(
            session,
            executor,
            new PollEventQuerier(new BoundedPollEventCollector(100)),
            "metrics");
    }

    @After
    public void postConditions() {
        Mockito.verifyNoMoreInteractions(executor, loggingTask, topicControl, updateControl);
    }

    @Test
    public void start() throws Exception {
        reporter.start();

        verify(topicControl).addTopic(eq("metrics/poll"), eq(JSON), addCallbackCaptor.capture());

        addCallbackCaptor.getValue().onTopicAdded("metrics/poll");
        verify(executor).scheduleAtFixedRate(isA(Runnable.class), eq(1L), eq(1L), eq(MINUTES));
    }

    @Test
    public void startTwice() throws Exception {
        start();

        reporter.start();
        verify(topicControl, times(2)).addTopic(eq("metrics/poll"), eq(JSON), addCallbackCaptor.capture());

        addCallbackCaptor.getValue().onTopicAdded("metrics/poll");
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