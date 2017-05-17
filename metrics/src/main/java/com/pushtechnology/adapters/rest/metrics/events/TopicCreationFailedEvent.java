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

import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;

/**
 * Event describing a failed topic creation.
 *
 * @author Matt Champion 17/05/2017
 */
public final class TopicCreationFailedEvent {
    private final TopicCreationRequestEvent requestEvent;
    private final TopicAddFailReason failReason;
    private final long failedTimestamp;

    /**
     * Constructor.
     */
    public TopicCreationFailedEvent(
            TopicCreationRequestEvent requestEvent,
            TopicAddFailReason failReason,
            long failedTimestamp) {

        this.requestEvent = requestEvent;
        this.failReason = failReason;
        this.failedTimestamp = failedTimestamp;
    }

    /**
     * @return the topic creation request event
     */
    public TopicCreationRequestEvent getRequestEvent() {
        return requestEvent;
    }

    /**
     * @return the failure reason
     */
    public TopicAddFailReason getFailReason() {
        return failReason;
    }

    /**
     * @return the failure timestamp
     */
    public long getFailedTimestamp() {
        return failedTimestamp;
    }

    /**
     * @return the time to failure
     */
    public long getRequestTime() {
        return failedTimestamp - requestEvent.getRequestTimestamp();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TopicCreationFailedEvent that = (TopicCreationFailedEvent) o;
        return failedTimestamp == that.failedTimestamp &&
            requestEvent.equals(that.requestEvent) &&
            failReason == that.failReason;
    }

    @Override
    public int hashCode() {
        int result = requestEvent.hashCode();
        result = 31 * result + failReason.hashCode();
        result = 31 * result + (int) (failedTimestamp ^ (failedTimestamp >>> 32));
        return result;
    }
}
