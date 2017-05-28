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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link TopicCreationEventCounter}.
 *
 * @author Matt Champion 28/05/2017
 */
public final class TopicCreationEventCounterTest {
    @Test
    public void onRequest() throws Exception {
        final TopicCreationEventCounter counter = new TopicCreationEventCounter();

        counter.onTopicCreationRequest(null, null);

        assertEquals(1, counter.getRequests());
        assertEquals(0, counter.getSuccesses());
        assertEquals(0, counter.getFailures());
    }

    @Test
    public void onRequestWithInitialData() throws Exception {
        final TopicCreationEventCounter counter = new TopicCreationEventCounter();

        counter.onTopicCreationRequest(null, null, null);

        assertEquals(1, counter.getRequests());
        assertEquals(0, counter.getSuccesses());
        assertEquals(0, counter.getFailures());
    }

    @Test
    public void onSuccess() throws Exception {
        final TopicCreationEventCounter counter = new TopicCreationEventCounter();

        counter.onTopicCreationRequest(null, null).onTopicCreated();

        assertEquals(1, counter.getRequests());
        assertEquals(1, counter.getSuccesses());
        assertEquals(0, counter.getFailures());
    }

    @Test
    public void onFailure() throws Exception {
        final TopicCreationEventCounter counter = new TopicCreationEventCounter();

        counter.onTopicCreationRequest(null, null).onTopicCreationFailed(null);

        assertEquals(1, counter.getRequests());
        assertEquals(0, counter.getSuccesses());
        assertEquals(1, counter.getFailures());
    }
}