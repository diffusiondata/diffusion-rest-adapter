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

import com.pushtechnology.adapters.rest.metrics.TopicCreationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationSuccessEvent;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * A bounded event collector for topic creation events.
 *
 * @author Matt Champion 24/05/2017
 */
@ThreadSafe
public final class BoundedTopicCreationEventCollector implements TopicCreationEventListener {

    @GuardedBy("this")
    private final List<TopicCreationRequestEvent> topicCreationRequestEvents = new ArrayList<>();
    @GuardedBy("this")
    private final List<TopicCreationSuccessEvent> topicCreationSuccessEvents = new ArrayList<>();
    @GuardedBy("this")
    private final List<TopicCreationFailedEvent> topicCreationFailedEvents = new ArrayList<>();
    private final int eventLimit;

    /**
     * Constructor.
     */
    public BoundedTopicCreationEventCollector() {
        this(100);
    }

    /**
     * Constructor.
     */
    /*package*/ BoundedTopicCreationEventCollector(int eventLimit) {
        this.eventLimit = eventLimit;
    }

    @Override
    public void onTopicCreationRequest(TopicCreationRequestEvent event) {
        topicCreationRequestEvents.add(event);

        while (topicCreationRequestEvents.size() > eventLimit) {
            topicCreationRequestEvents.remove(0);
        }
    }

    @Override
    public void onTopicCreationSuccess(TopicCreationSuccessEvent event) {
        topicCreationSuccessEvents.add(event);

        while (topicCreationSuccessEvents.size() > eventLimit) {
            topicCreationSuccessEvents.remove(0);
        }
    }

    @Override
    public void onTopicCreationFailed(TopicCreationFailedEvent event) {
        topicCreationFailedEvents.add(event);

        while (topicCreationFailedEvents.size() > eventLimit) {
            topicCreationFailedEvents.remove(0);
        }
    }

    /*package*/ synchronized List<TopicCreationRequestEvent> getTopicCreationRequestEvents() {
        return new ArrayList<>(topicCreationRequestEvents);
    }

    /*package*/ synchronized List<TopicCreationSuccessEvent> getTopicCreationSuccessEvents() {
        return new ArrayList<>(topicCreationSuccessEvents);
    }

    /*package*/ synchronized List<TopicCreationFailedEvent> getTopicCreationFailedEvents() {
        return new ArrayList<>(topicCreationFailedEvents);
    }
}
