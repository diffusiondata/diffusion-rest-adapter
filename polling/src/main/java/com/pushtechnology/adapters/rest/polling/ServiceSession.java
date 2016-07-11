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

import java.util.concurrent.ScheduledExecutorService;

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
        for (final Endpoint endpoint : service.getEndpoints()) {
            executor.schedule(new PollingTask(endpoint), 0, MILLISECONDS);
        }
    }

    /**
     * Start the session.
     */
    public synchronized void stop() {
        isRunning = false;
    }

    private final class PollingTask implements Runnable {
        private final Endpoint endpoint;

        public PollingTask(Endpoint endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void run() {
            synchronized (ServiceSession.this) {
                if (!isRunning) {
                    return;
                }
            }

            pollClient.request(
                service,
                endpoint,
                new FutureCallback<JSON>() {
                    @Override
                    public void completed(JSON json) {
                        LOG.trace("Polled value", json.toJsonString());

                        diffusionClient.publish(endpoint, json);

                        synchronized (ServiceSession.this) {
                            if (isRunning) {
                                executor.schedule(new PollingTask(endpoint), service.getPollPeriod(), MILLISECONDS);
                            }
                        }
                    }

                    @Override
                    public void failed(Exception e) {
                        e.printStackTrace();
                        synchronized (ServiceSession.this) {
                            if (isRunning) {
                                executor.schedule(new PollingTask(endpoint), service.getPollPeriod(), MILLISECONDS);
                            }
                        }
                    }

                    @Override
                    public void cancelled() {
                        synchronized (ServiceSession.this) {
                            if (isRunning) {
                                executor.schedule(new PollingTask(endpoint), service.getPollPeriod(), MILLISECONDS);
                            }
                        }
                    }
                });
        }
    }
}
