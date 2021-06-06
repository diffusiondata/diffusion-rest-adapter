/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TopicCreationEventCounter}.
 *
 * @author Push Technology Limited
 */
public final class TopicCreationEventCounterTest {
    @Test
    public void onRequest() throws Exception {
        final TopicCreationEventCounter counter = new TopicCreationEventCounter();

        counter.onTopicCreationRequest(null);

        assertEquals(1, counter.getRequests());
        assertEquals(0, counter.getSuccesses());
        assertEquals(0, counter.getFailures());
    }

    @Test
    public void onSuccess() throws Exception {
        final TopicCreationEventCounter counter = new TopicCreationEventCounter();

        counter.onTopicCreationRequest(null);
        counter.onTopicCreationSuccess(null);

        assertEquals(1, counter.getRequests());
        assertEquals(1, counter.getSuccesses());
        assertEquals(0, counter.getFailures());
    }

    @Test
    public void onFailure() throws Exception {
        final TopicCreationEventCounter counter = new TopicCreationEventCounter();

        counter.onTopicCreationRequest(null);
        counter.onTopicCreationFailed(null);

        assertEquals(1, counter.getRequests());
        assertEquals(0, counter.getSuccesses());
        assertEquals(1, counter.getFailures());
    }
}
