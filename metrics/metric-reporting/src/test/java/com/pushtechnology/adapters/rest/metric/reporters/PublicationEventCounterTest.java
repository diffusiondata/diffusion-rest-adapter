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

import static com.pushtechnology.diffusion.client.callbacks.ErrorReason.COMMUNICATION_FAILURE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;

/**
 * Unit tests for {@link PublicationEventCounter}.
 *
 * @author Push Technology Limited
 */
public final class PublicationEventCounterTest {
    @Test
    public void onRequest() throws Exception {
        final PublicationEventCounter counter = new PublicationEventCounter();

        counter.onPublicationRequest(PublicationRequestEvent.Factory.create("", 5));

        assertEquals(1, counter.getRequests());
        assertEquals(0, counter.getSuccesses());
        assertEquals(0, counter.getFailures());

        assertEquals(5, counter.getRequestBytes());
        assertEquals(0, counter.getSuccessBytes());
        assertEquals(0, counter.getFailedBytes());
    }

    @Test
    public void onSuccess() throws Exception {
        final PublicationEventCounter counter = new PublicationEventCounter();

        final PublicationRequestEvent requestEvent = PublicationRequestEvent.Factory.create("", 5);
        counter.onPublicationRequest(requestEvent);
        counter.onPublicationSuccess(PublicationSuccessEvent.Factory.create(requestEvent));

        assertEquals(1, counter.getRequests());
        assertEquals(1, counter.getSuccesses());
        assertEquals(0, counter.getFailures());

        assertEquals(5, counter.getRequestBytes());
        assertEquals(5, counter.getSuccessBytes());
        assertEquals(0, counter.getFailedBytes());

    }

    @Test
    public void onFailure() throws Exception {
        final PublicationEventCounter counter = new PublicationEventCounter();

        final PublicationRequestEvent requestEvent = PublicationRequestEvent.Factory.create("", 5);
        counter.onPublicationRequest(requestEvent);
        counter.onPublicationFailed(PublicationFailedEvent.Factory.create(requestEvent, COMMUNICATION_FAILURE));

        assertEquals(1, counter.getRequests());
        assertEquals(0, counter.getSuccesses());
        assertEquals(1, counter.getFailures());

        assertEquals(5, counter.getRequestBytes());
        assertEquals(0, counter.getSuccessBytes());
        assertEquals(5, counter.getFailedBytes());
    }
}
