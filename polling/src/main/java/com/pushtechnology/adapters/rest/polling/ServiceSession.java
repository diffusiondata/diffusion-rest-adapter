package com.pushtechnology.adapters.rest.polling;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.concurrent.FutureCallback;

import com.pushtechnology.adapters.rest.model.v3.Endpoint;
import com.pushtechnology.adapters.rest.model.v3.Service;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * The session for a REST service.
 *
 * @author Push Technology Limited
 */
public final class ServiceSession {
    private final ScheduledExecutorService executor;
    private final PollClient pollClient;
    private final Service service;
    private boolean isRunning = false;

    /**
     * Constructor.
     */
    public ServiceSession(ScheduledExecutorService executor, PollClient pollClient, Service service) {
        this.executor = executor;
        this.pollClient = pollClient;
        this.service = service;
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
            pollClient.request(
                service,
                endpoint,
                new FutureCallback<JSON>() {
                    @Override
                    public void completed(JSON json) {
                        System.out.println(json.toJsonString());

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
