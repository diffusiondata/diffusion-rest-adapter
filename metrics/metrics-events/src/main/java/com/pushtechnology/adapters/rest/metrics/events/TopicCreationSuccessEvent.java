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

/**
 * Event describing a successful topic creation.
 *
 * @author Matt Champion 17/05/2017
 */
public final class TopicCreationSuccessEvent {
    private final TopicCreationRequestEvent requestEvent;
    private final long successTimestamp;

    /**
     * Constructor.
     */
    public TopicCreationSuccessEvent(TopicCreationRequestEvent requestEvent, long successTimestamp) {
        this.requestEvent = requestEvent;
        this.successTimestamp = successTimestamp;
    }

    /**
     * @return the topic creation request event
     */
    public TopicCreationRequestEvent getRequestEvent() {
        return requestEvent;
    }

    /**
     * @return the success timestamp
     */
    public long getSuccessTimestamp() {
        return successTimestamp;
    }

    /**
     * @return the request timestamp
     */
    public long getRequestTime() {
        return successTimestamp - requestEvent.getRequestTimestamp();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TopicCreationSuccessEvent that = (TopicCreationSuccessEvent) o;
        return successTimestamp == that.successTimestamp &&
            requestEvent.equals(that.requestEvent);
    }

    @Override
    public int hashCode() {
        int result = requestEvent.hashCode();
        result = 31 * result + (int) (successTimestamp ^ (successTimestamp >>> 32));
        return result;
    }
}
