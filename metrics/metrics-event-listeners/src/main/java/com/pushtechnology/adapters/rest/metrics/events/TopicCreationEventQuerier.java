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

import java.math.BigDecimal;
import java.util.List;
import java.util.OptionalLong;

import com.pushtechnology.adapters.rest.metrics.TopicCreationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationSuccessEvent;

import net.jcip.annotations.ThreadSafe;

/**
 * A querier for topic creation events.
 *
 * @author Matt Champion 26/05/2017
 */
@ThreadSafe
public final class TopicCreationEventQuerier {
    private final BoundedTopicCreationEventCollector eventCollector;

    /**
     * Constructor.
     */
    public TopicCreationEventQuerier(BoundedTopicCreationEventCollector eventCollector) {
        this.eventCollector = eventCollector;
    }

    /**
     * @return the throughput of topic creation requests in events per second
     */
    public BigDecimal getTopicCreationRequestThroughput() {
        return getTopicCreationRequestThroughput(System.currentTimeMillis());
    }

    /*package*/ BigDecimal getTopicCreationRequestThroughput(long currentTimestamp) {
        final List<TopicCreationRequestEvent> requestEvents = eventCollector.getTopicCreationRequestEvents();

        final int numberOfEvents = requestEvents.size();
        final OptionalLong maybeMin = requestEvents.stream().mapToLong(TopicCreationRequestEvent::getRequestTimestamp).min();
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
        final List<TopicCreationSuccessEvent> successEvents = eventCollector.getTopicCreationSuccessEvents();

        return successEvents
            .stream()
            .mapToLong(TopicCreationSuccessEvent::getRequestTime)
            .max();
    }

    /**
     * @return the minimum successful request time in milliseconds
     */
    public OptionalLong getMinimumSuccessfulRequestTime() {
        final List<TopicCreationSuccessEvent> successEvents = eventCollector.getTopicCreationSuccessEvents();

        return successEvents
            .stream()
            .mapToLong(TopicCreationSuccessEvent::getRequestTime)
            .min();
    }

    /**
     * @return the throughput of topic creation failures in events per second
     */
    public BigDecimal getTopicCreationFailureThroughput() {
        return getTopicCreationFailureThroughput(System.currentTimeMillis());
    }

    /*package*/ BigDecimal getTopicCreationFailureThroughput(long currentTimestamp) {
        final List<TopicCreationFailedEvent> failedEvents = eventCollector.getTopicCreationFailedEvents();

        final int numberOfEvents = failedEvents.size();
        final OptionalLong maybeMin = failedEvents.stream().mapToLong(TopicCreationFailedEvent::getFailedTimestamp).min();
        if (maybeMin.isPresent()) {
            final long min = maybeMin.getAsLong();
            final long period = currentTimestamp - min;

            return BigDecimal.valueOf(numberOfEvents * 1000, 3).divide(BigDecimal.valueOf(period, 3), 3, HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
