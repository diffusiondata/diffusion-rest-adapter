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

import java.util.concurrent.atomic.AtomicLong;

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.event.listeners.PollEventListener;

/**
 * Implementation of {@link com.pushtechnology.adapters.rest.metrics.listeners.EventCounter} for poll events.
 *
 * @author Push Technology Limited
 */
public final class PollEventCounter extends AbstractEventCounter implements PollEventListener {
    private final AtomicLong responseBodyBytes = new AtomicLong();

    @Override
    public void onPollRequest(PollRequestEvent event) {
        onRequest();
    }

    @Override
    public void onPollSuccess(PollSuccessEvent event) {
        onSuccess();
        responseBodyBytes.addAndGet(event.getResponseLength());
    }

    @Override
    public void onPollFailed(PollFailedEvent event) {
        onFailure();
    }

    /**
     * @return the total number of bytes in the response bodies received
     */
    public long getTotalPollResponseBytes() {
        return responseBodyBytes.get();
    }
}
