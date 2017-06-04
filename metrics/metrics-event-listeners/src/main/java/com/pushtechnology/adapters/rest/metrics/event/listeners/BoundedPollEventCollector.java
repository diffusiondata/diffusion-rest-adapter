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

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;

import net.jcip.annotations.ThreadSafe;

/**
 * A bounded event collector for poll events.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class BoundedPollEventCollector
        extends AbstractBoundedEventCollector<PollRequestEvent, PollSuccessEvent, PollFailedEvent>
        implements PollEventListener {

    /**
     * Constructor.
     */
    public BoundedPollEventCollector(int eventLimit) {
        super(eventLimit);
    }

    @Override
    public void onPollRequest(PollRequestEvent event) {
        onRequest(event);
    }

    @Override
    public void onPollSuccess(PollSuccessEvent event) {
        onSuccess(event);
    }

    @Override
    public void onPollFailed(PollFailedEvent event) {
        onFailed(event);
    }
}
