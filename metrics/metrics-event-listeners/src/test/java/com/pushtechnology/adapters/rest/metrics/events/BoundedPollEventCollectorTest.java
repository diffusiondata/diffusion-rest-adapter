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

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;

/**
 * Unit tests for {@link BoundedPollEventCollector}.
 *
 * @author Matt Champion 24/05/2017
 */
public final class BoundedPollEventCollectorTest {

    private static final int EVENT_LIMIT = 100;

    private final PollRequestEvent pollRequestEvent = PollRequestEvent.Factory.create("http://example.org/hello", 100);
    private final PollSuccessEvent pollSuccessEvent = PollSuccessEvent.Factory.create(pollRequestEvent, 200, 0, 100);
    private final PollFailedEvent pollFailedEvent = PollFailedEvent.Factory.create(pollRequestEvent, new Exception("for test"), 100);

    private BoundedPollEventCollector eventCollector;

    @Before
    public void setUp() {
        eventCollector = new BoundedPollEventCollector(EVENT_LIMIT);
    }

    @Test
    public void onPollRequest() throws Exception {
        eventCollector.onPollRequest(pollRequestEvent);

        assertThat(eventCollector.getPollRequestEvents(), contains(pollRequestEvent));
    }

    @Test
    public void onPollSuccess() throws Exception {
        eventCollector.onPollSuccess(pollSuccessEvent);

        assertThat(eventCollector.getPollSuccessEvents(), contains(pollSuccessEvent));
    }

    @Test
    public void onPollFailed() throws Exception {
        eventCollector.onPollFailed(pollFailedEvent);

        assertThat(eventCollector.getPollFailedEvents(), contains(pollFailedEvent));
    }

    @Test
    public void pollRequestsLimited() throws Exception {
        for (int i = 0; i < EVENT_LIMIT * 2; i++) {
            eventCollector.onPollRequest(pollRequestEvent);
        }

        assertEquals(EVENT_LIMIT, eventCollector.getPollRequestEvents().size());
    }

    @Test
    public void pollSuccessLimited() throws Exception {
        for (int i = 0; i < EVENT_LIMIT * 2; i++) {
            eventCollector.onPollSuccess(pollSuccessEvent);
        }

        assertEquals(EVENT_LIMIT, eventCollector.getPollSuccessEvents().size());
    }

    @Test
    public void pollFailedLimited() throws Exception {
        for (int i = 0; i < EVENT_LIMIT * 2; i++) {
            eventCollector.onPollFailed(pollFailedEvent);
        }

        assertEquals(EVENT_LIMIT, eventCollector.getPollFailedEvents().size());
    }
}
