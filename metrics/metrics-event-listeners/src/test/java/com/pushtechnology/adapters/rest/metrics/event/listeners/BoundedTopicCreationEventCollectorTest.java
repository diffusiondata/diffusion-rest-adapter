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

package com.pushtechnology.adapters.rest.metrics.event.listeners;

import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.EXISTS;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.BINARY;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.pushtechnology.adapters.rest.metrics.TopicCreationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationSuccessEvent;

/**
 * Unit tests for {@link BoundedTopicCreationEventCollector}.
 *
 * @author Push Technology Limited
 */
public final class BoundedTopicCreationEventCollectorTest {

    private static final int EVENT_LIMIT = 100;

    private final TopicCreationRequestEvent publicationRequestEvent = TopicCreationRequestEvent.Factory.create("topic/path", BINARY, 100);
    private final TopicCreationSuccessEvent publicationSuccessEvent = TopicCreationSuccessEvent.Factory.create(publicationRequestEvent,100);
    private final TopicCreationFailedEvent publicationFailedEvent = TopicCreationFailedEvent.Factory.create(publicationRequestEvent, EXISTS, 100);

    private BoundedTopicCreationEventCollector eventCollector;

    @Before
    public void setUp() {
        eventCollector = new BoundedTopicCreationEventCollector(EVENT_LIMIT);
    }

    @Test
    public void onTopicCreationRequest() throws Exception {
        eventCollector.onTopicCreationRequest(publicationRequestEvent);

        assertThat(eventCollector.getRequestEvents(), contains(publicationRequestEvent));
    }

    @Test
    public void onTopicCreationSuccess() throws Exception {
        eventCollector.onTopicCreationSuccess(publicationSuccessEvent);

        assertThat(eventCollector.getSuccessEvents(), contains(publicationSuccessEvent));
    }

    @Test
    public void onTopicCreationFailed() throws Exception {
        eventCollector.onTopicCreationFailed(publicationFailedEvent);

        assertThat(eventCollector.getFailedEvents(), contains(publicationFailedEvent));
    }

    @Test
    public void topicCreationRequestLimited() throws Exception {
        for (int i = 0; i < EVENT_LIMIT * 2; i++) {
            eventCollector.onTopicCreationRequest(publicationRequestEvent);
        }

        assertEquals(EVENT_LIMIT, eventCollector.getRequestEvents().size());
    }

    @Test
    public void topicCreationSuccessLimited() throws Exception {
        for (int i = 0; i < EVENT_LIMIT * 2; i++) {
            eventCollector.onTopicCreationSuccess(publicationSuccessEvent);
        }

        assertEquals(EVENT_LIMIT, eventCollector.getSuccessEvents().size());
    }

    @Test
    public void topicCreationFailedLimited() throws Exception {
        for (int i = 0; i < EVENT_LIMIT * 2; i++) {
            eventCollector.onTopicCreationFailed(publicationFailedEvent);
        }

        assertEquals(EVENT_LIMIT, eventCollector.getFailedEvents().size());
    }
}
