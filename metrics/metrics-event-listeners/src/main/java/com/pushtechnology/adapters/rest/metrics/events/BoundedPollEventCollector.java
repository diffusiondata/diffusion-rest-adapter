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

import java.util.ArrayList;
import java.util.List;

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * A bounded event collector for poll events.
 *
 * @author Matt Champion 24/05/2017
 */
@ThreadSafe
public final class BoundedPollEventCollector implements PollEventListener {

    @GuardedBy("this")
    private final List<PollRequestEvent> pollRequestEvents = new ArrayList<>();
    @GuardedBy("this")
    private final List<PollSuccessEvent> pollSuccessEvents = new ArrayList<>();
    @GuardedBy("this")
    private final List<PollFailedEvent> pollFailedEvents = new ArrayList<>();
    private final int eventLimit;

    /**
     * Constructor.
     */
    public BoundedPollEventCollector() {
        this(100);
    }

    /**
     * Constructor.
     */
    /*package*/ BoundedPollEventCollector(int eventLimit) {
        this.eventLimit = eventLimit;
    }

    @Override
    public synchronized void onPollRequest(PollRequestEvent event) {
        pollRequestEvents.add(event);

        while (pollRequestEvents.size() > eventLimit) {
            pollRequestEvents.remove(0);
        }
    }

    @Override
    public synchronized void onPollSuccess(PollSuccessEvent event) {
        pollSuccessEvents.add(event);

        while (pollSuccessEvents.size() > eventLimit) {
            pollSuccessEvents.remove(0);
        }
    }

    @Override
    public synchronized void onPollFailed(PollFailedEvent event) {
        pollFailedEvents.add(event);

        while (pollFailedEvents.size() > eventLimit) {
            pollFailedEvents.remove(0);
        }
    }

    /*package*/ synchronized List<PollRequestEvent> getPollRequestEvents() {
        return new ArrayList<>(pollRequestEvents);
    }

    /*package*/ synchronized List<PollSuccessEvent> getPollSuccessEvents() {
        return new ArrayList<>(pollSuccessEvents);
    }

    /*package*/ synchronized List<PollFailedEvent> getPollFailedEvents() {
        return new ArrayList<>(pollFailedEvents);
    }
}
