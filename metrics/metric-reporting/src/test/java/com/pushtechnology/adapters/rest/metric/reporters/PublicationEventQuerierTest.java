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

import static com.pushtechnology.diffusion.client.callbacks.ErrorReason.ACCESS_DENIED;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPublicationEventCollector;

/**
 * Unit tests for {@link PublicationEventQuerier}.
 *
 * @author Matt Champion 26/05/2017
 */
public final class PublicationEventQuerierTest {

    private final PublicationRequestEvent requestEvent0 = PublicationRequestEvent.Factory.create("topic/path", 10, 100);
    private final PublicationRequestEvent requestEvent1 = PublicationRequestEvent.Factory.create("topic/path", 10, 150);
    private final PublicationRequestEvent requestEvent2 = PublicationRequestEvent.Factory.create("topic/path", 10, 200);
    private final PublicationSuccessEvent successEvent0 = PublicationSuccessEvent.Factory.create(requestEvent0, 200);
    private final PublicationSuccessEvent successEvent1 = PublicationSuccessEvent.Factory.create(requestEvent1, 250);
    private final PublicationSuccessEvent successEvent = PublicationSuccessEvent.Factory.create(requestEvent2, 300);
    private final PublicationFailedEvent failureEvent0 = PublicationFailedEvent.Factory.create(requestEvent0, ACCESS_DENIED, 2000);
    private final PublicationFailedEvent failureEvent1 = PublicationFailedEvent.Factory.create(requestEvent1, ACCESS_DENIED, 2500);

    private PublicationEventQuerier publicationEventQuerier;

    @Before
    public void setUp() {
        final BoundedPublicationEventCollector eventCollector = new BoundedPublicationEventCollector(100);
        eventCollector.onPublicationRequest(requestEvent0);
        eventCollector.onPublicationRequest(requestEvent1);
        eventCollector.onPublicationRequest(requestEvent2);
        eventCollector.onPublicationSuccess(successEvent0);
        eventCollector.onPublicationSuccess(successEvent1);
        eventCollector.onPublicationSuccess(successEvent);
        eventCollector.onPublicationFailed(failureEvent0);
        eventCollector.onPublicationFailed(failureEvent1);

        publicationEventQuerier = new PublicationEventQuerier(eventCollector);
    }

    @Test
    public void getPublicationRequestThroughput() {
        final BigDecimal requestThroughput = publicationEventQuerier.getRequestThroughput(200);

        assertEquals(BigDecimal.valueOf(30000, 3), requestThroughput);
    }

    @Test
    public void getMaximumSuccessfulRequestTime() {
        assertEquals(100, publicationEventQuerier.getMaximumSuccessfulRequestTime().getAsLong());
    }

    @Test
    public void getMinimumSuccessfulRequestTime() {
        assertEquals(100, publicationEventQuerier.getMinimumSuccessfulRequestTime().getAsLong());
    }

    @Test
    public void getPublicationFailureThroughput() {
        final BigDecimal failureThroughput = publicationEventQuerier.getFailureThroughput(2500);

        assertEquals(BigDecimal.valueOf(4000, 3), failureThroughput);
    }
}
