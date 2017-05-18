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

package com.pushtechnology.adapters.rest.metrics;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assert.assertEquals;
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

/**
 * Unit tests for {@link SimpleCountingMetricsCollector}.
 *
 * @author Matt Champion 14/05/2017
 */
public final class SimpleCountingMetricsCollectorTest {
    @Mock
    private ScheduledExecutorService executor;
    @Mock
    private ScheduledFuture loggingTask;

    private SimpleCountingMetricsCollector collector;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        initMocks(this);

        when(executor.scheduleAtFixedRate(isA(Runnable.class), isA(Long.class), isA(Long.class), isA(TimeUnit.class)))
            .thenReturn(loggingTask);

        collector = new SimpleCountingMetricsCollector(executor);
    }

    @After
    public void postConditions() {
        Mockito.verifyNoMoreInteractions(executor, loggingTask);
    }

    @Test
    public void start() throws Exception {
        collector.start();

        verify(executor).scheduleAtFixedRate(isA(Runnable.class), eq(1L), eq(1L), eq(MINUTES));
    }

    @Test
    public void startTwice() throws Exception {
        start();

        collector.start();
    }

    @Test
    public void close() throws Exception {
        start();

        collector.close();

        verify(loggingTask).cancel(false);
    }

    @Test
    public void closeBeforeStart() throws Exception {
        collector.close();
    }

    @Test
    public void onPollRequest() throws Exception {
        collector.onPollRequest(null, null);

        assertEquals(1, collector.getPollRequests());
    }

    @Test
    public void onTopicCreationRequest() throws Exception {
        collector.onTopicCreationRequest(null, null);

        assertEquals(1, collector.getTopicCreationRequests());
    }

    @Test
    public void onTopicCreationRequestWithValue() throws Exception {
        collector.onTopicCreationRequest(null, null, null);

        assertEquals(1, collector.getTopicCreationRequests());
    }

    @Test
    public void onPublicationRequest() throws Exception {
        collector.onPublicationRequest(null, null, null);

        assertEquals(1, collector.getPublicationRequests());
    }

    @Test
    public void onPollResponse() throws Exception {
        collector.onPollResponse(null);

        assertEquals(1, collector.getPollSuccesses());
    }

    @Test
    public void onPollFailure() throws Exception {
        collector.onPollFailure(null);

        assertEquals(1, collector.getPollFailures());
    }

    @Test
    public void onPublication() throws Exception {
        collector.onPublication(null);

        assertEquals(1, collector.getPublicationSuccesses());
    }

    @Test
    public void onPublicationFailed() throws Exception {
        collector.onPublicationFailed(null, null);

        assertEquals(1, collector.getPublicationFailures());
    }

    @Test
    public void onTopicCreated() throws Exception {
        collector.onTopicCreated();

        assertEquals(1, collector.getTopicCreationSuccesses());
    }

    @Test
    public void onTopicCreationFailed() throws Exception {
        collector.onTopicCreationFailed(null);

        assertEquals(1, collector.getTopicCreationFailures());
    }
}
