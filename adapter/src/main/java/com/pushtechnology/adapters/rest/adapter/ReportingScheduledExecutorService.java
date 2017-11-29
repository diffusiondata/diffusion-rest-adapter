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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A decorating scheduled executor service that logs any runtime exceptions.
 *
 * @author Push Technology Limited
 */
/*package*/ final class ReportingScheduledExecutorService implements ScheduledExecutorService {
    private static final Logger LOG = LoggerFactory.getLogger(ReportingScheduledExecutorService.class);
    private final ScheduledExecutorService executorService;

    /*package*/ ReportingScheduledExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public ScheduledFuture<?> schedule(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
        return executorService.schedule(decorate(command), delay, unit);
    }

    @NotNull
    @Override
    public <V> ScheduledFuture<V> schedule(@NotNull Callable<V> callable, long delay, @NotNull TimeUnit unit) {
        return executorService.schedule(decorate(callable), delay, unit);
    }

    @NotNull
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(
        @NotNull Runnable command,
        long initialDelay,
        long period,
        @NotNull TimeUnit unit) {
        return executorService.scheduleAtFixedRate(decorate(command), initialDelay, period, unit);
    }

    @NotNull
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(
        @NotNull Runnable command,
        long initialDelay,
        long delay,
        @NotNull TimeUnit unit) {
        return executorService.scheduleWithFixedDelay(decorate(command), initialDelay, delay, unit);
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @NotNull
    @Override
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    @NotNull
    @Override
    public <T> Future<T> submit(@NotNull Callable<T> task) {
        return executorService.submit(decorate(task));
    }

    @NotNull
    @Override
    public <T> Future<T> submit(@NotNull Runnable task, T result) {
        return executorService.submit(decorate(task), result);
    }

    @NotNull
    @Override
    public Future<?> submit(@NotNull Runnable task) {
        return executorService.submit(decorate(task));
    }

    @NotNull
    @Override
    public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(decorateAll(tasks));
    }

    @NotNull
    @Override
    public <T> List<Future<T>> invokeAll(
        @NotNull Collection<? extends Callable<T>> tasks,
        long timeout,
        @NotNull TimeUnit unit) throws InterruptedException {

        return executorService.invokeAll(decorateAll(tasks), timeout, unit);
    }

    @NotNull
    @Override
    public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks) throws
        InterruptedException,
        ExecutionException {

        return executorService.invokeAny(decorateAll(tasks));
    }

    @Override
    public <T> T invokeAny(
        @NotNull Collection<? extends Callable<T>> tasks,
        long timeout,
        @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

        return executorService.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        executorService.execute(decorate(command));
    }

    private Runnable decorate(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            }
            // CHECKSTYLE.OFF: IllegalCatch
            catch (RuntimeException e) {
                LOG.error("Runtime exception thrown by executor task", e);
                throw e;
            }
            // CHECKSTYLE.ON: IllegalCatch
        };
    }

    private <V> Callable<V> decorate(Callable<V> callable) {
        return () -> {
            try {
                return callable.call();
            }
            // CHECKSTYLE.OFF: IllegalCatch
            catch (RuntimeException e) {
                LOG.error("Runtime exception thrown by executor task", e);
                throw e;
            }
            // CHECKSTYLE.ON: IllegalCatch
        };
    }

    private <V> Collection<? extends Callable<V>> decorateAll(Collection<? extends Callable<V>> tasks) {
        final List<Callable<V>> decoratedTasks = new ArrayList<>(tasks.size());
        tasks.forEach(task -> decoratedTasks.add(decorate(task)));
        return decoratedTasks;
    }
}
