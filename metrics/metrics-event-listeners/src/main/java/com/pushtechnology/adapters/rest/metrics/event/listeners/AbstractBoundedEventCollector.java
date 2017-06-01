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

package com.pushtechnology.adapters.rest.metrics.event.listeners;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Abstract implementation of {@link EventCollector} that collects the most recent of a set number of events.
 *
 * @param <R> the type of request events
 * @param <S> the type of success events
 * @param <F> the type of failure events
 * @author Push Technology Limited
 */
@ThreadSafe
public abstract class AbstractBoundedEventCollector<R, S, F>
        implements EventCollector<R, S, F> {
    @GuardedBy("this")
    private final List<R> topicCreationRequestEvents = new ArrayList<>();
    @GuardedBy("this")
    private final List<S> topicCreationSuccessEvents = new ArrayList<>();
    @GuardedBy("this")
    private final List<F> topicCreationFailedEvents = new ArrayList<>();
    private final int eventLimit;

    /**
     * Constructor.
     */
    protected AbstractBoundedEventCollector(int eventLimit) {
        this.eventLimit = eventLimit;
    }

    /**
     * Notified when there is a request event.
     */
    protected synchronized void onRequest(R event) {
        topicCreationRequestEvents.add(event);

        while (topicCreationRequestEvents.size() > eventLimit) {
            topicCreationRequestEvents.remove(0);
        }
    }

    /**
     * Notified when there is a success event.
     */
    protected synchronized void onSuccess(S event) {
        topicCreationSuccessEvents.add(event);

        while (topicCreationSuccessEvents.size() > eventLimit) {
            topicCreationSuccessEvents.remove(0);
        }
    }

    /**
     * Notified when there is a failed event.
     */
    protected synchronized void onFailed(F event) {
        topicCreationFailedEvents.add(event);

        while (topicCreationFailedEvents.size() > eventLimit) {
            topicCreationFailedEvents.remove(0);
        }
    }

    @Override
    public synchronized List<R> getRequestEvents() {
        return new ArrayList<>(topicCreationRequestEvents);
    }

    @Override
    public synchronized List<S> getSuccessEvents() {
        return new ArrayList<>(topicCreationSuccessEvents);
    }

    @Override
    public synchronized List<F> getFailedEvents() {
        return new ArrayList<>(topicCreationFailedEvents);
    }
}
