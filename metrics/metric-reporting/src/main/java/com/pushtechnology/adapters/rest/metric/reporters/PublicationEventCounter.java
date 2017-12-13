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

import java.util.concurrent.atomic.AtomicInteger;

import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.event.listeners.PublicationEventListener;

/**
 * Implementation of {@link com.pushtechnology.adapters.rest.metrics.listeners.EventCounter} for publication events.
 *
 * @author Push Technology Limited
 */
public final class PublicationEventCounter extends AbstractEventCounter implements PublicationEventListener {
    private final AtomicInteger requestBytes = new AtomicInteger();
    private final AtomicInteger successBytes = new AtomicInteger();
    private final AtomicInteger failedBytes = new AtomicInteger();

    @Override
    public void onPublicationRequest(PublicationRequestEvent event) {
        onRequest();
        requestBytes.addAndGet(event.getUpdateLength());
    }

    @Override
    public void onPublicationSuccess(PublicationSuccessEvent event) {
        onSuccess();
        successBytes.addAndGet(event.getRequestEvent().getUpdateLength());
    }

    @Override
    public void onPublicationFailed(PublicationFailedEvent event) {
        onFailure();
        failedBytes.addAndGet(event.getRequestEvent().getUpdateLength());
    }

    /**
     * @return the requested publication bytes
     */
    public int getRequestBytes() {
        return requestBytes.get();
    }

    /**
     * @return the successful publication bytes
     */
    public int getSuccessBytes() {
        return successBytes.get();
    }

    /**
     * @return the failed publication bytes
     */
    public int getFailedBytes() {
        return failedBytes.get();
    }
}
