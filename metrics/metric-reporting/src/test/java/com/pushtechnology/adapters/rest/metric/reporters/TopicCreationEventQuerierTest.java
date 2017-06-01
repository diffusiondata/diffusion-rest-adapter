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

import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.EXISTS_MISMATCH;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.BINARY;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.pushtechnology.adapters.rest.metrics.TopicCreationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedTopicCreationEventCollector;

/**
 * Unit tests for {@link TopicCreationEventQuerier}.
 *
 * @author Push Technology Limited
 */
public final class TopicCreationEventQuerierTest {

    private final TopicCreationRequestEvent requestEvent0 = TopicCreationRequestEvent.Factory.create("topic/path", BINARY, 10, 100);
    private final TopicCreationRequestEvent requestEvent1 = TopicCreationRequestEvent.Factory.create("topic/path", BINARY, 10, 150);
    private final TopicCreationRequestEvent requestEvent2 = TopicCreationRequestEvent.Factory.create("topic/path", BINARY, 10, 200);
    private final TopicCreationSuccessEvent successEvent0 = TopicCreationSuccessEvent.Factory.create(requestEvent0, 200);
    private final TopicCreationSuccessEvent successEvent1 = TopicCreationSuccessEvent.Factory.create(requestEvent1, 250);
    private final TopicCreationSuccessEvent successEvent = TopicCreationSuccessEvent.Factory.create(requestEvent2, 300);
    private final TopicCreationFailedEvent failureEvent0 = TopicCreationFailedEvent.Factory.create(requestEvent0, EXISTS_MISMATCH, 2000);
    private final TopicCreationFailedEvent failureEvent1 = TopicCreationFailedEvent.Factory.create(requestEvent1, EXISTS_MISMATCH, 2500);

    private TopicCreationEventQuerier eventQuerier;

    @Before
    public void setUp() {
        final BoundedTopicCreationEventCollector eventCollector = new BoundedTopicCreationEventCollector(100);
        eventCollector.onTopicCreationRequest(requestEvent0);
        eventCollector.onTopicCreationRequest(requestEvent1);
        eventCollector.onTopicCreationRequest(requestEvent2);
        eventCollector.onTopicCreationSuccess(successEvent0);
        eventCollector.onTopicCreationSuccess(successEvent1);
        eventCollector.onTopicCreationSuccess(successEvent);
        eventCollector.onTopicCreationFailed(failureEvent0);
        eventCollector.onTopicCreationFailed(failureEvent1);

        eventQuerier = new TopicCreationEventQuerier(eventCollector);
    }

    @Test
    public void getTopicCreationRequestThroughput() {
        final BigDecimal requestThroughput = eventQuerier.getRequestThroughput(200);

        assertEquals(BigDecimal.valueOf(30000, 3), requestThroughput);
    }

    @Test
    public void getMaximumSuccessfulRequestTime() {
        assertEquals(100, eventQuerier.getMaximumSuccessfulRequestTime().getAsLong());
    }

    @Test
    public void getMinimumSuccessfulRequestTime() {
        assertEquals(100, eventQuerier.getMinimumSuccessfulRequestTime().getAsLong());
    }

    @Test
    public void getTopicCreationFailureThroughput() {
        final BigDecimal failureThroughput = eventQuerier.getFailureThroughput(2500);

        assertEquals(BigDecimal.valueOf(4000, 3), failureThroughput);
    }
}
