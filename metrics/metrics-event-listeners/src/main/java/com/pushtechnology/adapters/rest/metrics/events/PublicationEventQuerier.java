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

import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;

import net.jcip.annotations.ThreadSafe;

/**
 * A querier for publication events.
 *
 * @author Matt Champion 26/05/2017
 */
@ThreadSafe
public final class PublicationEventQuerier {
    private final BoundedPublicationEventCollector publicationEventCollector;

    /**
     * Constructor.
     */
    public PublicationEventQuerier(BoundedPublicationEventCollector publicationEventCollector) {
        this.publicationEventCollector = publicationEventCollector;
    }

    /**
     * @return the throughput of publication requests in events per second
     */
    public BigDecimal getPublicationRequestThroughput() {
        return getPublicationRequestThroughput(System.currentTimeMillis());
    }

    /*package*/ BigDecimal getPublicationRequestThroughput(long currentTimestamp) {
        final List<PublicationRequestEvent> requestEvents = publicationEventCollector.getPublicationRequestEvents();

        final int numberOfEvents = requestEvents.size();
        final OptionalLong maybeMin = requestEvents.stream().mapToLong(PublicationRequestEvent::getRequestTimestamp).min();
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
        final List<PublicationSuccessEvent> successEvents = publicationEventCollector.getPublicationSuccessEvents();

        return successEvents
            .stream()
            .mapToLong(PublicationSuccessEvent::getRequestTime)
            .max();
    }

    /**
     * @return the minimum successful request time in milliseconds
     */
    public OptionalLong getMinimumSuccessfulRequestTime() {
        final List<PublicationSuccessEvent> successEvents = publicationEventCollector.getPublicationSuccessEvents();

        return successEvents
            .stream()
            .mapToLong(PublicationSuccessEvent::getRequestTime)
            .min();
    }

    /**
     * @return the throughput of publication failures in events per second
     */
    public BigDecimal getPublicationFailureThroughput() {
        return getPublicationFailureThroughput(System.currentTimeMillis());
    }

    /*package*/ BigDecimal getPublicationFailureThroughput(long currentTimestamp) {
        final List<PublicationFailedEvent> failedEvents = publicationEventCollector.getPublicationFailedEvents();

        final int numberOfEvents = failedEvents.size();
        final OptionalLong maybeMin = failedEvents.stream().mapToLong(PublicationFailedEvent::getFailedTimestamp).min();
        if (maybeMin.isPresent()) {
            final long min = maybeMin.getAsLong();
            final long period = currentTimestamp - min;

            return BigDecimal.valueOf(numberOfEvents * 1000, 3).divide(BigDecimal.valueOf(period, 3), 3, HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
