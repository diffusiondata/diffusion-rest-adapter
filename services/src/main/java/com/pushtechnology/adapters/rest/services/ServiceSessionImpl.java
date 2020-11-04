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

package com.pushtechnology.adapters.rest.services;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointClient;
import com.pushtechnology.adapters.rest.polling.EndpointPollHandlerFactory;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Implementation of {@link ServiceSession}. Access to the endpoints is synchronised.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class ServiceSessionImpl implements ServiceSession {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceSessionImpl.class);
    @GuardedBy("this")
    private final Map<EndpointConfig, PollHandle> endpointPollers = new HashMap<>();
    private final ScheduledExecutorService executor;
    private final EndpointClient endpointClient;
    private final ServiceConfig serviceConfig;
    private final EndpointPollHandlerFactory handlerFactory;
    private final TopicManagementClient topicManagementClient;
    @GuardedBy("this")
    private boolean isRunning;

    /**
     * Constructor.
     */
    public ServiceSessionImpl(
            ScheduledExecutorService executor,
            EndpointClient endpointClient,
            ServiceConfig serviceConfig,
            EndpointPollHandlerFactory handlerFactory,
            TopicManagementClient topicManagementClient) {

        this.executor = executor;
        this.endpointClient = endpointClient;
        this.serviceConfig = serviceConfig;
        this.handlerFactory = handlerFactory;
        this.topicManagementClient = topicManagementClient;
    }

    @Override
    public synchronized void start() {
        isRunning = true;

        LOG.debug("Starting service session {}", serviceConfig);
        endpointPollers.replaceAll((endpoint, currentHandle) -> startEndpoint(endpoint));
    }

    @Override
    public synchronized void addEndpoint(EndpointConfig endpointConfig) {
        if (endpointPollers.containsKey(endpointConfig)) {
            return;
        }

        endpointPollers.put(endpointConfig, isRunning ? startEndpoint(endpointConfig) : null);
    }

    private PollHandle startEndpoint(EndpointConfig endpointConfig) {
        assert endpointPollers.get(endpointConfig) == null : "The endpoint has already been started";

        final BiConsumer<EndpointResponse, Throwable> handler =
            new PollResultHandler(handlerFactory.create(serviceConfig, endpointConfig));

        final Future<?> future;
        if (serviceConfig.getPollPeriod() > 0) {
            future = executor.scheduleWithFixedDelay(
                new PollingTask(endpointConfig, handler),
                    serviceConfig.getPollPeriod(),
                    serviceConfig.getPollPeriod(),
                    MILLISECONDS);
        }
        else {
            future = CompletableFuture.completedFuture(new Object());
        }

        return new PollHandle(future);
    }

    private void stopEndpoint(EndpointConfig endpointConfig, PollHandle pollHandle) {
        topicManagementClient.removeEndpoint(serviceConfig, endpointConfig);
        if (pollHandle != null) {
            pollHandle.taskHandle.cancel(false);
            if (pollHandle.currentPollHandle != null) {
                pollHandle.currentPollHandle.cancel(false);
            }
        }
    }

    @Override
    public synchronized void stop() {
        isRunning = false;

        endpointPollers.replaceAll((endpointConfig, pollHandle) -> {
            stopEndpoint(endpointConfig, pollHandle);
            return null;
        });

        LOG.debug("Stopping service session {}", serviceConfig);
    }

    /**
     * The polling task. Triggers an asynchronous poll request.
     */
    private final class PollingTask implements Runnable {
        private final EndpointConfig endpointConfig;
        private final BiConsumer<EndpointResponse, Throwable> handler;

        PollingTask(EndpointConfig endpointConfig, BiConsumer<EndpointResponse, Throwable> handler) {
            this.endpointConfig = endpointConfig;
            this.handler = handler;
        }

        @Override
        public void run() {
            synchronized (ServiceSessionImpl.this) {
                endpointPollers
                    .get(endpointConfig)
                    .currentPollHandle = endpointClient
                    .request(
                        serviceConfig,
                        endpointConfig)
                    .whenComplete(handler);
            }
        }
    }

    /**
     * The handler for the polling result. Notifies the publishing client of the new data.
     */
    private final class PollResultHandler implements BiConsumer<EndpointResponse, Throwable> {
        private final BiConsumer<EndpointResponse, Throwable> delegate;

        private PollResultHandler(BiConsumer<EndpointResponse, Throwable> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void accept(EndpointResponse response, Throwable throwable) {
            synchronized (ServiceSessionImpl.this) {
                if (isRunning) {
                    delegate.accept(response, throwable);
                }
            }
        }
    }

    /**
     * Represent a poll. Holds a handle to the task triggering a poll and a handle to the outstanding poll.
     */
    private static final class PollHandle {
        private final Future<?> taskHandle;
        @GuardedBy("ServiceSession.this")
        private Future<?> currentPollHandle;

        private PollHandle(Future<?> taskHandle) {
            this.taskHandle = taskHandle;
            currentPollHandle = null;
        }
    }
}
