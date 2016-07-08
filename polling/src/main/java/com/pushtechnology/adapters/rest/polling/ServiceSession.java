package com.pushtechnology.adapters.rest.polling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.concurrent.FutureCallback;

import com.pushtechnology.adapters.rest.model.v2.Endpoint;
import com.pushtechnology.adapters.rest.model.v2.Service;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * The session for a REST service.
 *
 * @author Push Technology Limited
 */
public final class ServiceSession {
    private final Collection<Future<?>> tasks = new ArrayList<>();
    private final ScheduledExecutorService executor;
    private final PollClient pollClient;
    private final Service service;

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
        for (final Endpoint endpoint : service.getEndpoints()) {
            tasks.add(
                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        pollClient.request(
                            service,
                            endpoint,
                            new FutureCallback<JSON>() {
                                @Override
                                public void completed(JSON json) {
                                    System.out.println(json.toJsonString());
                                }

                                @Override
                                public void failed(Exception e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void cancelled() {
                                }
                            });
                    }
                },
                service.getPollPeriod(),
                TimeUnit.MILLISECONDS));
        }
    }

    /**
     * Start the session.
     */
    public synchronized void stop() {
        final Iterator<Future<?>> taskIterator = tasks.iterator();
        while (taskIterator.hasNext()) {
            taskIterator.next().cancel(false);
            taskIterator.remove();
        }
    }
}
