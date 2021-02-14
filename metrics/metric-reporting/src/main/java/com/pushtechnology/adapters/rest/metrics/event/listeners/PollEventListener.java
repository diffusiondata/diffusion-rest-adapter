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

package com.pushtechnology.adapters.rest.metrics.event.listeners;

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;

/**
 * Listener for events about polling.
 *
 * @author Push Technology Limited
 */
public interface PollEventListener {
    /**
     * Notified when an attempt to poll an endpoint made.
     *
     * @param event the event
     */
    void onPollRequest(PollRequestEvent event);

    /**
     * Notified when a poll completes.
     *
     * @param event the event
     */
    void onPollSuccess(PollSuccessEvent event);

    /**
     * Notified when a poll fails.
     *
     * @param event the event
     */
    void onPollFailed(PollFailedEvent event);
}
