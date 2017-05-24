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

import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * A bounded event collector for publication events.
 *
 * @author Matt Champion 24/05/2017
 */
@ThreadSafe
public final class BoundedPublicationEventCollector implements PublicationEventListener {

    @GuardedBy("this")
    private final List<PublicationRequestEvent> publicationRequestEvents = new ArrayList<>();
    @GuardedBy("this")
    private final List<PublicationSuccessEvent> publicationSuccessEvents = new ArrayList<>();
    @GuardedBy("this")
    private final List<PublicationFailedEvent> publicationFailedEvents = new ArrayList<>();
    private final int eventLimit;

    /**
     * Constructor.
     */
    public BoundedPublicationEventCollector() {
        this(100);
    }

    /**
     * Constructor.
     */
    /*package*/ BoundedPublicationEventCollector(int eventLimit) {
        this.eventLimit = eventLimit;
    }

    @Override
    public void onPublicationRequest(PublicationRequestEvent event) {
        publicationRequestEvents.add(event);

        while (publicationRequestEvents.size() > eventLimit) {
            publicationRequestEvents.remove(0);
        }
    }

    @Override
    public void onPublicationSuccess(PublicationSuccessEvent event) {
        publicationSuccessEvents.add(event);

        while (publicationSuccessEvents.size() > eventLimit) {
            publicationSuccessEvents.remove(0);
        }
    }

    @Override
    public void onPublicationFailed(PublicationFailedEvent event) {
        publicationFailedEvents.add(event);

        while (publicationFailedEvents.size() > eventLimit) {
            publicationFailedEvents.remove(0);
        }
    }

    /*package*/ synchronized List<PublicationRequestEvent> getPublicationRequestEvents() {
        return new ArrayList<>(publicationRequestEvents);
    }

    /*package*/ synchronized List<PublicationSuccessEvent> getPublicationSuccessEvents() {
        return new ArrayList<>(publicationSuccessEvents);
    }

    /*package*/ synchronized List<PublicationFailedEvent> getPublicationFailedEvents() {
        return new ArrayList<>(publicationFailedEvents);
    }
}
