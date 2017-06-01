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

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPollEventCollector;

/**
 * Unit tests for {@link PollEventQuerier}.
 *
 * @author Push Technology Limited
 */
public final class PollEventQuerierTest {

    private final PollRequestEvent pollRequestEvent0 = PollRequestEvent.Factory.create("http://example.org/hello", 100);
    private final PollRequestEvent pollRequestEvent1 = PollRequestEvent.Factory.create("http://example.org/hello", 150);
    private final PollRequestEvent pollRequestEvent2 = PollRequestEvent.Factory.create("http://example.org/hello", 200);
    private final PollSuccessEvent pollSuccessEvent0 = PollSuccessEvent.Factory.create(pollRequestEvent0, 200, 100, 200);
    private final PollSuccessEvent pollSuccessEvent1 = PollSuccessEvent.Factory.create(pollRequestEvent1, 200, 100, 250);
    private final PollSuccessEvent pollSuccessEvent2 = PollSuccessEvent.Factory.create(pollRequestEvent2, 200, 100, 300);
    private final PollFailedEvent pollFailureEvent0 = PollFailedEvent.Factory.create(pollRequestEvent0, new Exception("for test"), 2000);
    private final PollFailedEvent pollFailureEvent1 = PollFailedEvent.Factory.create(pollRequestEvent1, new Exception("for test"), 2500);

    private PollEventQuerier pollEventQuerier;

    @Before
    public void setUp() {
        final BoundedPollEventCollector pollEventCollector = new BoundedPollEventCollector(100);
        pollEventCollector.onPollRequest(pollRequestEvent0);
        pollEventCollector.onPollRequest(pollRequestEvent1);
        pollEventCollector.onPollRequest(pollRequestEvent2);
        pollEventCollector.onPollSuccess(pollSuccessEvent0);
        pollEventCollector.onPollSuccess(pollSuccessEvent1);
        pollEventCollector.onPollSuccess(pollSuccessEvent2);
        pollEventCollector.onPollFailed(pollFailureEvent0);
        pollEventCollector.onPollFailed(pollFailureEvent1);

        pollEventQuerier = new PollEventQuerier(pollEventCollector);
    }

    @Test
    public void getPollRequestThroughput() {
        final BigDecimal requestThroughput = pollEventQuerier.getRequestThroughput(200);

        assertEquals(BigDecimal.valueOf(30000, 3), requestThroughput);
    }

    @Test
    public void getMaximumSuccessfulRequestTime() {
        assertEquals(100, pollEventQuerier.getMaximumSuccessfulRequestTime().getAsLong());
    }

    @Test
    public void getMinimumSuccessfulRequestTime() {
        assertEquals(100, pollEventQuerier.getMinimumSuccessfulRequestTime().getAsLong());
    }

    @Test
    public void getStatusCodes() {
        final Map<Integer, Integer> statusCodes = pollEventQuerier.getStatusCodes();
        assertEquals(1, statusCodes.size());
        assertEquals(3, (int) statusCodes.get(200));
    }

    @Test
    public void getPollFailureThroughput() {
        final BigDecimal failureThroughput = pollEventQuerier.getFailureThroughput(2500);

        assertEquals(BigDecimal.valueOf(4000, 3), failureThroughput);
    }
}
