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

import com.pushtechnology.diffusion.client.topics.details.TopicType;

/**
 * Event describing a topic creation request.
 *
 * @author Matt Champion 17/05/2017
 */
public final class TopicCreationRequestEvent {
    private final String path;
    private final TopicType topicType;
    private final int initialValueLength;
    private final long requestTimestamp;

    /**
     * Constructor.
     */
    public TopicCreationRequestEvent(String path, TopicType topicType, int initialValueLength, long requestTimestamp) {
        this.path = path;
        this.topicType = topicType;
        this.initialValueLength = initialValueLength;
        this.requestTimestamp = requestTimestamp;
    }

    /**
     * @return the topic path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the topic type
     */
    public TopicType getTopicType() {
        return topicType;
    }

    /**
     * @return the length of any initial value
     */
    public int getInitialValueLength() {
        return initialValueLength;
    }

    /**
     * @return the request timestamp
     */
    public long getRequestTimestamp() {
        return requestTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TopicCreationRequestEvent that = (TopicCreationRequestEvent) o;
        return initialValueLength == that.initialValueLength &&
            requestTimestamp == that.requestTimestamp &&
            path.equals(that.path) &&
            topicType == that.topicType;
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + topicType.hashCode();
        result = 31 * result + initialValueLength;
        result = 31 * result + (int) (requestTimestamp ^ (requestTimestamp >>> 32));
        return result;
    }
}
