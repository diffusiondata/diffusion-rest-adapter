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

import com.pushtechnology.adapters.rest.metrics.TopicCreationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationSuccessEvent;

import net.jcip.annotations.ThreadSafe;

/**
 * A querier for topic creation events.
 *
 * @author Matt Champion 26/05/2017
 */
@ThreadSafe
public final class TopicCreationEventQuerier
        extends CommonEventQuerier<TopicCreationRequestEvent, TopicCreationSuccessEvent, TopicCreationFailedEvent> {

    /**
     * Constructor.
     */
    public TopicCreationEventQuerier(BoundedTopicCreationEventCollector eventCollector) {
        super(eventCollector);
    }
}