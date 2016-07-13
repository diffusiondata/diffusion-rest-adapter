/*******************************************************************************
 * Copyright (C) 2016 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.polling;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.diffusion.datatype.json.JSON;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * The session for a REST service.
 * <p>
 * Supports multiple endpoints and dynamically changing the endpoints. Access to the endpoints is synchronised.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class ServiceSession {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceSession.class);
    @GuardedBy("this")
    private final Map<EndpointConfig, PollHandle> endpointPollers = new HashMap<>();
    private final ScheduledExecutorService executor;
    private final PollClient pollClient;
    private final ServiceConfig serviceConfig;
    private final PublishingClient diffusionClient;
    @GuardedBy("this")
    private boolean isRunning = false;

    /**
     * Constructor.
     */
    public ServiceSession(
            ScheduledExecutorService executor,
            PollClient pollClient,
            ServiceConfig serviceConfig,
            PublishingClient diffusionClient) {
        this.executor = executor;
        this.pollClient = pollClient;
        this.serviceConfig = serviceConfig;
        this.diffusionClient = diffusionClient;
    }

    /**
     * Start the session.
     */
    public synchronized void start() {
        isRunning = true;

        endpointPollers.replaceAll((endpoint, currentHandle) -> startEndpoint(endpoint));
    }

    /**
     * Add an endpoint to the session.
     */
    public synchronized void addEndpoint(EndpointConfig endpointConfig) {
        if (endpointPollers.containsKey(endpointConfig)) {
            return;
        }

        endpointPollers.put(endpointConfig, isRunning ? startEndpoint(endpointConfig) : null);
    }

    private PollHandle startEndpoint(EndpointConfig endpointConfig) {
        assert endpointPollers.get(endpointConfig) == null : "The endpoint has already been started";

        final PollResultHandler handler = new PollResultHandler(endpointConfig);
        final ScheduledFuture<?> future = executor.scheduleWithFixedDelay(
            new PollingTask(endpointConfig, handler),
            0L,
            serviceConfig.getPollPeriod(),
            MILLISECONDS);

        return new PollHandle(future);
    }

    /**
     * Remove an endpoint from the session.
     */
    public synchronized void removeEndpoint(EndpointConfig endpointConfig) {
        assert endpointPollers.containsKey(endpointConfig) : "The endpoint has not been added";

        stopEndpoint(endpointPollers.remove(endpointConfig));

    }

    private void stopEndpoint(PollHandle pollHandle) {
        if (pollHandle != null) {
            pollHandle.taskHandle.cancel(false);
            if (pollHandle.currentPollHandle != null) {
                pollHandle.currentPollHandle.cancel(false);
            }
        }
    }

    /**
     * Start the session.
     */
    public synchronized void stop() {
        isRunning = false;

        final Iterator<Map.Entry<EndpointConfig, PollHandle>> iterator = endpointPollers.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<EndpointConfig, PollHandle> entry = iterator.next();
            stopEndpoint(entry.getValue());
            iterator.remove();
        }
    }

    /**
     * The polling task. Triggers an asynchronous poll request.
     */
    private final class PollingTask implements Runnable {
        private final EndpointConfig endpointConfig;
        private final PollResultHandler handler;

        public PollingTask(EndpointConfig endpointConfig, PollResultHandler handler) {
            this.endpointConfig = endpointConfig;
            this.handler = handler;
        }

        @Override
        public void run() {
            synchronized (ServiceSession.this) {
                endpointPollers.get(endpointConfig).currentPollHandle = pollClient.request(
                    serviceConfig,
                    endpointConfig,
                    handler);
            }
        }
    }

    /**
     * The handler for the polling result. Notifies the publishing client of the new data.
     */
    private final class PollResultHandler implements FutureCallback<JSON> {
        private final EndpointConfig endpointConfig;

        private PollResultHandler(EndpointConfig endpointConfig) {
            this.endpointConfig = endpointConfig;
        }

        @Override
        public void completed(JSON json) {
            LOG.trace("Polled value {}", json.toJsonString());

            synchronized (ServiceSession.this) {
                if (isRunning) {
                    diffusionClient.publish(serviceConfig, endpointConfig, json);
                }
            }
        }

        @Override
        public void failed(Exception e) {
            LOG.warn("Poll failed", e);
        }

        @Override
        public void cancelled() {
            LOG.warn("Poll cancelled");
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
