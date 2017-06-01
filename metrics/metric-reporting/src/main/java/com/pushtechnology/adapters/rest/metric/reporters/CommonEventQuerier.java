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

package com.pushtechnology.adapters.rest.metric.reporters;

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;
import java.util.List;
import java.util.OptionalLong;

import com.pushtechnology.adapters.rest.metrics.FailureEvent;
import com.pushtechnology.adapters.rest.metrics.RequestEvent;
import com.pushtechnology.adapters.rest.metrics.SuccessEvent;
import com.pushtechnology.adapters.rest.metrics.event.listeners.EventCollector;

import net.jcip.annotations.ThreadSafe;

/**
 * A querier for infomation common to different types of events.
 *
 * @param <R> the type of request events
 * @param <S> the type of success events
 * @param <F> the type of failure events
 * @author Push Technology Limited
 */
@ThreadSafe
public abstract class CommonEventQuerier<R extends RequestEvent, S extends SuccessEvent, F extends FailureEvent> {
    private final EventCollector<R, S, F> eventCollector;

    /**
     * Constructor.
     */
    public CommonEventQuerier(EventCollector<R, S, F> eventCollector) {
        this.eventCollector = eventCollector;
    }

    /**
     * @return the event collector
     */
    protected EventCollector<R, S, F> getEventCollector() {
        return eventCollector;
    }

    /**
     * @return the throughput of requests in events per second
     */
    public BigDecimal getRequestThroughput() {
        return getRequestThroughput(System.currentTimeMillis());
    }

    /*package*/ BigDecimal getRequestThroughput(long currentTimestamp) {
        final List<R> requestEvents = eventCollector.getRequestEvents();

        final int numberOfEvents = requestEvents.size();
        final OptionalLong maybeMin = requestEvents.stream().mapToLong(RequestEvent::getRequestTimestamp).min();
        if (maybeMin.isPresent()) {
            final long min = maybeMin.getAsLong();
            final long period = currentTimestamp - min;

            return BigDecimal.valueOf(numberOfEvents * 1000, 3).divide(BigDecimal.valueOf(period, 3), 3, HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * @return the maximum successful request time in milliseconds
     */
    public OptionalLong getMaximumSuccessfulRequestTime() {
        final List<S> successEvents = eventCollector.getSuccessEvents();

        return successEvents
            .stream()
            .mapToLong(SuccessEvent::getRequestTime)
            .max();
    }

    /**
     * @return the minimum successful request time in milliseconds
     */
    public OptionalLong getMinimumSuccessfulRequestTime() {
        final List<S> successEvents = eventCollector.getSuccessEvents();

        return successEvents
            .stream()
            .mapToLong(SuccessEvent::getRequestTime)
            .min();
    }

    /**
     * @return the throughput failures in events per second
     */
    public BigDecimal getFailureThroughput() {
        return getFailureThroughput(System.currentTimeMillis());
    }

    /*package*/ BigDecimal getFailureThroughput(long currentTimestamp) {
        final List<F> failedEvents = eventCollector.getFailedEvents();

        final int numberOfEvents = failedEvents.size();
        final OptionalLong maybeMin = failedEvents.stream().mapToLong(FailureEvent::getFailedTimestamp).min();
        if (maybeMin.isPresent()) {
            final long min = maybeMin.getAsLong();
            final long period = currentTimestamp - min;

            return BigDecimal.valueOf(numberOfEvents * 1000, 3).divide(BigDecimal.valueOf(period, 3), 3, HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
