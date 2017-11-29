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

package com.pushtechnology.adapters.rest.adapter;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Unit tests for {@link ReportingScheduledExecutorService}.
 *
 * @author Push Technology Limited
 */
public final class ReportingScheduledExecutorServiceTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ScheduledExecutorService delegate;

    @Mock
    private Callable<Object> callable;

    @Mock
    private Runnable runnable;

    private ReportingScheduledExecutorService executorService;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        executorService = new ReportingScheduledExecutorService(delegate);

        when(delegate.submit(isNotNull(Runnable.class))).thenReturn(mock(Future.class));
        when(delegate.submit(isNotNull(Callable.class))).thenReturn(mock(Future.class));
        when(delegate.submit(isNotNull(Runnable.class), isNotNull())).thenReturn(mock(Future.class));
        when(delegate.schedule(isNotNull(Callable.class), anyLong(), isNotNull())).thenReturn(mock(ScheduledFuture.class));
        when(delegate.scheduleWithFixedDelay(isNotNull(), anyLong(), anyLong(), isNotNull())).thenReturn(mock(ScheduledFuture.class));
        when(delegate.scheduleAtFixedRate(isNotNull(), anyLong(), anyLong(), isNotNull())).thenReturn(mock(ScheduledFuture.class));
        when(delegate.invokeAny(isNotNull())).thenReturn(mock(Future.class));
    }

    @Test
    public void schedule() throws Exception {
        executorService.schedule(runnable, 1, TimeUnit.MINUTES);

        verify(delegate).schedule(isNotNull(Runnable.class), eq(1L), eq(TimeUnit.MINUTES));
    }

    @Test
    public void schedule1() throws Exception {
        executorService.schedule(callable, 1, TimeUnit.MINUTES);

        verify(delegate).schedule(isNotNull(Callable.class), eq(1L), eq(TimeUnit.MINUTES));
    }

    @Test
    public void scheduleAtFixedRate() throws Exception {
        executorService.scheduleAtFixedRate(runnable, 1, 2, TimeUnit.MINUTES);

        verify(delegate).scheduleAtFixedRate(isNotNull(), eq(1L), eq(2L), eq(TimeUnit.MINUTES));
    }

    @Test
    public void scheduleWithFixedDelay() throws Exception {
        executorService.scheduleWithFixedDelay(runnable, 1, 2, TimeUnit.MINUTES);

        verify(delegate).scheduleWithFixedDelay(isNotNull(), eq(1L), eq(2L), eq(TimeUnit.MINUTES));
    }

    @Test
    public void shutdown() throws Exception {
        executorService.shutdown();

        verify(delegate).shutdown();
    }

    @Test
    public void shutdownNow() throws Exception {
        executorService.shutdownNow();

        verify(delegate).shutdownNow();
    }

    @Test
    public void isShutdown() throws Exception {
        executorService.isShutdown();

        verify(delegate).isShutdown();
    }

    @Test
    public void isTerminated() throws Exception {
        executorService.isTerminated();

        verify(delegate).isTerminated();
    }

    @Test
    public void awaitTermination() throws Exception {
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        verify(delegate).awaitTermination(1, TimeUnit.MINUTES);
    }

    @Test
    public void submit() throws Exception {
        executorService.submit(runnable);

        verify(delegate).submit(isNotNull(Runnable.class));
    }

    @Test
    public void submit1() throws Exception {
        executorService.submit(callable);

        verify(delegate).submit(isNotNull(Callable.class));
    }

    @Test
    public void submit2() throws Exception {
        final Object o = new Object();
        executorService.submit(runnable, o);

        verify(delegate).submit(isNotNull(), eq(o));
    }

    @Test
    public void invokeAll() throws Exception {
        executorService.invokeAll(Collections.singleton(callable));

        verify(delegate).invokeAll(isNotNull());
    }

    @Test
    public void invokeAll1() throws Exception {
        executorService.invokeAll(Collections.singleton(callable), 1, TimeUnit.MINUTES);

        verify(delegate).invokeAll(isNotNull(), eq(1L), eq(TimeUnit.MINUTES));
    }

    @Test
    public void invokeAny() throws Exception {
        executorService.invokeAny(Collections.singleton(callable));

        verify(delegate).invokeAny(isNotNull());
    }

    @Test
    public void invokeAny1() throws Exception {
        executorService.invokeAny(Collections.singleton(callable), 1, TimeUnit.MINUTES);

        verify(delegate).invokeAny(isNotNull(), eq(1L), eq(TimeUnit.MINUTES));
    }

    @Test
    public void execute() throws Exception {
        executorService.execute(runnable);

        verify(delegate).execute(isNotNull());
    }
}
