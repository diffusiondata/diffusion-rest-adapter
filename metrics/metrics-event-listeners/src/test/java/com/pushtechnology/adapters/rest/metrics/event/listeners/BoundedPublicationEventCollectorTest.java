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

import static com.pushtechnology.diffusion.client.callbacks.ErrorReason.ACCESS_DENIED;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;

/**
 * Unit tests for {@link BoundedPublicationEventCollector}.
 *
 * @author Push Technology Limited
 */
public final class BoundedPublicationEventCollectorTest {

    private static final int EVENT_LIMIT = 100;

    private final PublicationRequestEvent publicationRequestEvent = PublicationRequestEvent.Factory.create("topic/path", 0, 100);
    private final PublicationSuccessEvent publicationSuccessEvent = PublicationSuccessEvent.Factory.create(publicationRequestEvent,100);
    private final PublicationFailedEvent publicationFailedEvent = PublicationFailedEvent.Factory.create(publicationRequestEvent, ACCESS_DENIED, 100);

    private BoundedPublicationEventCollector eventCollector;

    @Before
    public void setUp() {
        eventCollector = new BoundedPublicationEventCollector(EVENT_LIMIT);
    }

    @Test
    public void onPublicationRequest() throws Exception {
        eventCollector.onPublicationRequest(publicationRequestEvent);

        assertThat(eventCollector.getRequestEvents(), contains(publicationRequestEvent));
    }

    @Test
    public void onPublicationSuccess() throws Exception {
        eventCollector.onPublicationSuccess(publicationSuccessEvent);

        assertThat(eventCollector.getSuccessEvents(), contains(publicationSuccessEvent));
    }

    @Test
    public void onPublicationFailed() throws Exception {
        eventCollector.onPublicationFailed(publicationFailedEvent);

        assertThat(eventCollector.getFailedEvents(), contains(publicationFailedEvent));
    }

    @Test
    public void publicationRequestLimited() throws Exception {
        for (int i = 0; i < EVENT_LIMIT * 2; i++) {
            eventCollector.onPublicationRequest(publicationRequestEvent);
        }

        assertEquals(EVENT_LIMIT, eventCollector.getRequestEvents().size());
    }

    @Test
    public void publicationSuccessLimited() throws Exception {
        for (int i = 0; i < EVENT_LIMIT * 2; i++) {
            eventCollector.onPublicationSuccess(publicationSuccessEvent);
        }

        assertEquals(EVENT_LIMIT, eventCollector.getSuccessEvents().size());
    }

    @Test
    public void publicationFailedLimited() throws Exception {
        for (int i = 0; i < EVENT_LIMIT * 2; i++) {
            eventCollector.onPublicationFailed(publicationFailedEvent);
        }

        assertEquals(EVENT_LIMIT, eventCollector.getFailedEvents().size());
    }
}
