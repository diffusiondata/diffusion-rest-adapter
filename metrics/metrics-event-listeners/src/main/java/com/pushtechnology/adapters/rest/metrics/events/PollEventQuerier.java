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

package com.pushtechnology.adapters.rest.metrics.events;

import static java.math.RoundingMode.HALF_UP;
import static java.util.function.Function.identity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;

import net.jcip.annotations.ThreadSafe;

/**
 * A querier for poll events.
 *
 * @author Matt Champion 24/05/2017
 */
@ThreadSafe
public final class PollEventQuerier {
    private final BoundedPollEventCollector pollEventCollector;

    /**
     * Constructor.
     */
    public PollEventQuerier(BoundedPollEventCollector pollEventCollector) {
        this.pollEventCollector = pollEventCollector;
    }

    /**
     * @return the throughput of poll requests in events per second
     */
    public BigDecimal getPollRequestThroughput() {
        return getPollRequestThroughput(System.currentTimeMillis());
    }

    /*package*/ BigDecimal getPollRequestThroughput(long currentTimestamp) {
        final List<PollRequestEvent> requestEvents = pollEventCollector.getPollRequestEvents();

        final int numberOfEvents = requestEvents.size();
        final OptionalLong maybeMin = requestEvents.stream().mapToLong(PollRequestEvent::getRequestTimestamp).min();
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
        final List<PollSuccessEvent> successEvents = pollEventCollector.getPollSuccessEvents();

        return successEvents
            .stream()
            .mapToLong(PollSuccessEvent::getRequestTime)
            .max();
    }

    /**
     * @return the minimum successful request time in milliseconds
     */
    public OptionalLong getMinimumSuccessfulRequestTime() {
        final List<PollSuccessEvent> successEvents = pollEventCollector.getPollSuccessEvents();

        return successEvents
            .stream()
            .mapToLong(PollSuccessEvent::getRequestTime)
            .min();
    }

    /**
     * @return the count of status codes received
     */
    public Map<Integer, Integer> getStatusCodes() {
        return pollEventCollector
            .getPollSuccessEvents()
            .stream()
            .map(PollSuccessEvent::getStatusCode)
            .collect(Collectors.toMap(identity(), code -> 1, (l, r) -> l + r, HashMap::new));
    }

    /**
     * @return the throughput of poll failures in events per second
     */
    public BigDecimal getPollFailureThroughput() {
        return getPollFailureThroughput(System.currentTimeMillis());
    }

    /*package*/ BigDecimal getPollFailureThroughput(long currentTimestamp) {
        final List<PollFailedEvent> failedEvents = pollEventCollector.getPollFailedEvents();

        final int numberOfEvents = failedEvents.size();
        final OptionalLong maybeMin = failedEvents.stream().mapToLong(PollFailedEvent::getFailedTimestamp).min();
        if (maybeMin.isPresent()) {
            final long min = maybeMin.getAsLong();
            final long period = currentTimestamp - min;

            return BigDecimal.valueOf(numberOfEvents * 1000, 3).divide(BigDecimal.valueOf(period, 3), 3, HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
