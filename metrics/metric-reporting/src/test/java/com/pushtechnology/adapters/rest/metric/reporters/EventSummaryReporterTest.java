package com.pushtechnology.adapters.rest.metric.reporters;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPollEventCollector;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPublicationEventCollector;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedTopicCreationEventCollector;

/**
 * Unit tests for {@link EventSummaryReporter}.
 *
 * @author Matt Champion 24/05/2017
 */
public final class EventSummaryReporterTest {
    @Mock
    private ScheduledExecutorService executor;
    @Mock
    private ScheduledFuture loggingTask;

    private EventSummaryReporter reporter;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        initMocks(this);

        when(executor.scheduleAtFixedRate(isA(Runnable.class), isA(Long.class), isA(Long.class), isA(TimeUnit.class)))
            .thenReturn(loggingTask);

        reporter = new EventSummaryReporter(
            executor,
            new PollEventQuerier(new BoundedPollEventCollector(100)),
            new PublicationEventQuerier(new BoundedPublicationEventCollector(100)),
            new TopicCreationEventQuerier(new BoundedTopicCreationEventCollector(100)));
    }

    @After
    public void postConditions() {
        Mockito.verifyNoMoreInteractions(executor, loggingTask);
    }

    @Test
    public void start() throws Exception {
        reporter.start();

        verify(executor).scheduleAtFixedRate(isA(Runnable.class), eq(1L), eq(1L), eq(MINUTES));
    }

    @Test
    public void startTwice() throws Exception {
        start();

        reporter.start();
    }

    @Test
    public void close() throws Exception {
        start();

        reporter.close();

        verify(loggingTask).cancel(false);
    }

    @Test
    public void closeBeforeStart() throws Exception {
        reporter.close();
    }
}