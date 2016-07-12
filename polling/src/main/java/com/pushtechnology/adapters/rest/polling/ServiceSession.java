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
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.PublishingClient;
import com.pushtechnology.adapters.rest.model.latest.Endpoint;
import com.pushtechnology.adapters.rest.model.latest.Service;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * The session for a REST service.
 *
 * @author Push Technology Limited
 */
public final class ServiceSession {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceSession.class);
    private final Map<Endpoint, Future<?>> endpointPollers = new HashMap<>();
    private final ScheduledExecutorService executor;
    private final PollClient pollClient;
    private final Service service;
    private final PublishingClient diffusionClient;
    private boolean isRunning = false;

    /**
     * Constructor.
     */
    public ServiceSession(
            ScheduledExecutorService executor,
            PollClient pollClient,
            Service service,
            PublishingClient diffusionClient) {
        this.executor = executor;
        this.pollClient = pollClient;
        this.service = service;
        this.diffusionClient = diffusionClient;
    }

    /**
     * Start the session.
     */
    public synchronized void start() {
        isRunning = true;

        service.getEndpoints().forEach(this::startEndpoint);
    }

    /**
     * Start polling an endpoint.
     */
    public synchronized void startEndpoint(Endpoint endpoint) {
        assert !endpointPollers.containsKey(endpoint) : "The endpoint has already been started";
        final ScheduledFuture<?> future = executor.scheduleWithFixedDelay(
            new PollingTask(endpoint),
            0L,
            service.getPollPeriod(),
            MILLISECONDS);
        endpointPollers.put(endpoint, future);
    }

    /**
     * Stop polling an endpoint.
     */
    public synchronized void stopEndpoint(Endpoint endpoint) {
        assert endpointPollers.containsKey(endpoint) : "The endpoint has not been started";
        endpointPollers.get(endpoint).cancel(false);
    }

    /**
     * Start the session.
     */
    public synchronized void stop() {
        isRunning = false;
        service.getEndpoints().forEach(this::stopEndpoint);
    }

    private final class PollingTask implements Runnable {
        private final Endpoint endpoint;

        public PollingTask(Endpoint endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void run() {
            pollClient.request(
                service,
                endpoint,
                new FutureCallback<JSON>() {
                    @Override
                    public void completed(JSON json) {
                        LOG.trace("Polled value {}", json.toJsonString());

                        synchronized (ServiceSession.this) {
                            if (isRunning) {
                                diffusionClient.publish(endpoint, json);
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
                });
        }
    }
}
