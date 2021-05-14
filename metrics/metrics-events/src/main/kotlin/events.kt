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

package com.pushtechnology.adapters.rest.metrics;

import com.pushtechnology.diffusion.client.callbacks.ErrorReason
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason
import com.pushtechnology.diffusion.client.topics.details.TopicType
import java.lang.Exception

/**
 * Event describing a requested action.
 *
 * @author Push Technology Limited
 */
interface RequestEvent {
    /**
     * @return the request timestamp
     */
    val requestTimestamp: Long
}

/**
 * Event describing the success of an action.
 *
 * @author Push Technology Limited
 */
interface SuccessEvent {
    /**
     * @return the success timestamp
     */
    val successTimestamp: Long
    /**
     * @return the time taken for the request to succeed
     */
    val requestTime: Long
}

/**
 * Event describing the failure of an action.
 *
 * @author Push Technology Limited
 */
interface FailureEvent {
    /**
     * @return the failed timestamp
     */
    val failedTimestamp: Long
    /**
     * @return the time taken for the request to fail
     */
    val requestTime: Long
}

/**
 * Event describing a topic creation request.
 *
 * @author Push Technology Limited
 */
interface TopicCreationRequestEvent : RequestEvent {
    /**
     * @return the topic path
     */
    val path: String
    /**
     * @return the topic type
     */
    val topicType: TopicType

    /**
     * Factory for {@link TopicCreationRequestEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link TopicCreationRequestEvent}
         */
        fun create(path: String, topicType: TopicType): TopicCreationRequestEvent {
            return TopicCreationRequestEventImpl(path, topicType, System.currentTimeMillis())
        }

        /**
         * @return a new instance of {@link TopicCreationRequestEvent}
         */
        fun create(path: String, topicType: TopicType, timestamp: Long): TopicCreationRequestEvent {
            return TopicCreationRequestEventImpl(path, topicType, timestamp)
        }
    }
}

/**
 * Implementation of {@link TopicCreationRequestEvent}.
 *
 * @author Push Technology Limited
 */
private data class TopicCreationRequestEventImpl(
    override val path: String,
    override val topicType: TopicType,
    override val requestTimestamp: Long): TopicCreationRequestEvent

/**
 * Event describing a successful topic creation.
 *
 * @author Push Technology Limited
 */
interface TopicCreationSuccessEvent : SuccessEvent {
    /**
     * @return the topic creation request event
     */
    val requestEvent: TopicCreationRequestEvent

    /**
     * Factory for {@link TopicCreationSuccessEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link TopicCreationSuccessEvent}
         */
        fun create(requestEvent: TopicCreationRequestEvent): TopicCreationSuccessEvent {
            return TopicCreationSuccessEventImpl(requestEvent, System.currentTimeMillis())
        }

        /**
         * @return a new instance of {@link TopicCreationSuccessEvent}
         */
        fun create(requestEvent: TopicCreationRequestEvent, timestamp: Long): TopicCreationSuccessEvent {
            return TopicCreationSuccessEventImpl(requestEvent, timestamp)
        }
    }
}

/**
 * Event describing a successful topic creation.
 *
 * @author Push Technology Limited
 */
private data class TopicCreationSuccessEventImpl(
        override val requestEvent: TopicCreationRequestEvent,
        override val successTimestamp: Long) : TopicCreationSuccessEvent {

    override val requestTime: Long
        get() = successTimestamp - requestEvent.requestTimestamp
}

/**
 * Event describing a failed topic creation.
 *
 * @author Push Technology Limited
 */
interface TopicCreationFailedEvent : FailureEvent {
    /**
     * @return the topic creation request event
     */
    val requestEvent: TopicCreationRequestEvent
    /**
     * @return the failure reason
     */
    val failReason: TopicAddFailReason

    /**
     * Factory for {@link TopicCreationFailedEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link TopicCreationFailedEvent}
         */
        fun create(requestEvent: TopicCreationRequestEvent, failReason: TopicAddFailReason): TopicCreationFailedEvent {
            return TopicCreationFailedEventImpl(requestEvent, failReason, System.currentTimeMillis())
        }

        /**
         * @return a new instance of {@link TopicCreationFailedEvent}
         */
        fun create(requestEvent: TopicCreationRequestEvent, failReason: TopicAddFailReason, timestamp: Long): TopicCreationFailedEvent {
            return TopicCreationFailedEventImpl(requestEvent, failReason, timestamp)
        }
    }
}

/**
 * Event describing a failed topic creation.
 *
 * @author Push Technology Limited
 */
private data class TopicCreationFailedEventImpl(
        override val requestEvent: TopicCreationRequestEvent,
        override val failReason: TopicAddFailReason,
        override val failedTimestamp: Long) : TopicCreationFailedEvent {

    override val requestTime: Long
        get() = failedTimestamp - requestEvent.requestTimestamp
}

/**
 * Event describing a poll request.
 *
 * @author Push Technology Limited
 */
interface PollRequestEvent : RequestEvent {
    /**
     * @return the URI
     */
    val uri: String

    /**
     * Factory for {@link PollRequestEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link PollRequestEvent}
         */
        fun create(uri: String): PollRequestEvent {
            return PollRequestEventImpl(uri, System.currentTimeMillis())
        }

        /**
         * @return a new instance of {@link PollRequestEvent}
         */
        fun create(uri: String, timestamp: Long): PollRequestEvent {
            return PollRequestEventImpl(uri, timestamp)
        }
    }
}

/**
 * Event describing a poll request.
 *
 * @author Push Technology Limited
 */
private data class PollRequestEventImpl(
    override val uri: String,
    override val requestTimestamp: Long) : PollRequestEvent

/**
 * Event describing a successful poll request.
 *
 * @author Push Technology Limited
 */
interface PollSuccessEvent : SuccessEvent {
    /**
     * @return the poll request event
     */
    val requestEvent: PollRequestEvent
    /**
     * @return the status code of the response
     */
    val statusCode: Int
    /**
     * @return the length of the response
     */
    val responseLength: Long

    /**
     * Factory for {@link PollSuccessEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link PollSuccessEvent}
         */
        fun create(requestEvent: PollRequestEvent, statusCode: Int, responseLength: Long): PollSuccessEvent {
            return PollSuccessEventImpl(requestEvent, statusCode, responseLength, System.currentTimeMillis())
        }

        /**
         * @return a new instance of {@link PollSuccessEvent}
         */
        fun create(requestEvent: PollRequestEvent, statusCode: Int, responseLength: Long, timestamp: Long): PollSuccessEvent {
            return PollSuccessEventImpl(requestEvent, statusCode, responseLength, timestamp)
        }
    }
}

/**
 * Event describing a successful poll request.
 *
 * @author Push Technology Limited
 */
private data class PollSuccessEventImpl(
        override val requestEvent: PollRequestEvent,
        override val statusCode: Int,
        override val responseLength: Long,
        override val successTimestamp: Long) : PollSuccessEvent {

    override val requestTime: Long
        get() = successTimestamp - requestEvent.requestTimestamp
}

/**
 * Event describing a failed poll.
 *
 * @author Push Technology Limited
 */
interface PollFailedEvent : FailureEvent {
    /**
     * @return the poll request event
     */
    val requestEvent: PollRequestEvent
    /**
     * @return the exception
     */
    val exception: Throwable

    /**
     * Factory for {@link PollFailedEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link PollFailedEvent}
         */
        fun create(requestEvent: PollRequestEvent, exception: Throwable): PollFailedEvent {
            return PollFailedEventImpl(requestEvent, exception, System.currentTimeMillis())
        }

        /**
         * @return a new instance of {@link PollFailedEvent}
         */
        fun create(requestEvent: PollRequestEvent, exception: Throwable, timestamp: Long): PollFailedEvent {
            return PollFailedEventImpl(requestEvent, exception, timestamp)
        }
    }
}

/**
 * Event describing a failed poll.
 *
 * @author Push Technology Limited
 */
private data class PollFailedEventImpl(
        override val requestEvent: PollRequestEvent,
        override val exception: Throwable,
        override val failedTimestamp: Long) : PollFailedEvent {

    override val requestTime: Long
        get() = failedTimestamp - requestEvent.requestTimestamp
}

/**
 * Event describing a publication request.
 *
 * @author Push Technology Limited
 */
interface PublicationRequestEvent : RequestEvent {
    /**
     * @return the topic path
     */
    val path: String
    /**
     * @return the length of the update
     */
    val updateLength: Int

    /**
     * Factory for {@link PublicationRequestEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link PublicationRequestEvent}
         */
        fun create(path: String, updateLength: Int): PublicationRequestEvent {
            return PublicationRequestEventImpl(path, updateLength, System.currentTimeMillis())
        }

        /**
         * @return a new instance of {@link PublicationRequestEvent}
         */
        fun create(path: String, updateLength: Int, timestamp: Long): PublicationRequestEvent {
            return PublicationRequestEventImpl(path, updateLength, timestamp)
        }
    }
}

/**
 * Event describing a publication request.
 *
 * @author Push Technology Limited
 */
private data class PublicationRequestEventImpl(
    /**
     * @return the topic path
     */
    override val path: String,
    /**
     * @return the length of the value
     */
    override val updateLength: Int,
    /**
     * @return the request timestamp
     */
    override val requestTimestamp: Long) : PublicationRequestEvent

/**
 * Event describing a successful publication.
 *
 * @author Push Technology Limited
 */
interface PublicationSuccessEvent : SuccessEvent {
    /**
     * @return the publication request event
     */
    val requestEvent: PublicationRequestEvent

    /**
     * Factory for {@link PublicationSuccessEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link PublicationSuccessEvent}
         */
        fun create(requestEvent: PublicationRequestEvent): PublicationSuccessEvent {
            return PublicationSuccessEventImpl(requestEvent, System.currentTimeMillis())
        }

        /**
         * @return a new instance of {@link PublicationSuccessEvent}
         */
        fun create(requestEvent: PublicationRequestEvent, timestamp: Long): PublicationSuccessEvent {
            return PublicationSuccessEventImpl(requestEvent, timestamp)
        }
    }
}

/**
 * Event describing a successful publication.
 *
 * @author Push Technology Limited
 */
private data class PublicationSuccessEventImpl(
        override val requestEvent: PublicationRequestEvent,
        override val successTimestamp: Long) : PublicationSuccessEvent {

    override val requestTime: Long
        get() = successTimestamp - requestEvent.requestTimestamp
}

/**
 * Event describing a failed publication.
 *
 * @author Push Technology Limited
 */
interface PublicationFailedEvent : FailureEvent {
    /**
     * @return the publication request event
     */
    val requestEvent: PublicationRequestEvent
    /**
     * @return the error reason
     */
    val errorReason: ErrorReason

    /**
     * Factory for {@link PublicationFailedEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link PublicationFailedEvent}
         */
        fun create(requestEvent: PublicationRequestEvent, errorReason: ErrorReason): PublicationFailedEvent {
            return PublicationFailedEventImpl(requestEvent, errorReason, System.currentTimeMillis())
        }

        /**
         * @return a new instance of {@link PublicationFailedEvent}
         */
        fun create(requestEvent: PublicationRequestEvent, errorReason: ErrorReason, timestamp: Long): PublicationFailedEvent {
            return PublicationFailedEventImpl(requestEvent, errorReason, timestamp)
        }
    }
}

/**
 * Event describing a failed publication.
 *
 * @author Push Technology Limited
 */
private data class PublicationFailedEventImpl(
        override val requestEvent: PublicationRequestEvent,
        override val errorReason: ErrorReason,
        override val failedTimestamp: Long) : PublicationFailedEvent {

    override val requestTime: Long
        get() = failedTimestamp - requestEvent.requestTimestamp
}
