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
 * Event describing a topic creation request.
 *
 * @author Matt Champion 17/05/2017
 */
interface TopicCreationRequestEvent {
    /**
     * @return the topic path
     */
    val path: String
    /**
     * @return the topic type
     */
    val topicType: TopicType
    /**
     * @return the length of any initial value
     */
    val initialValueLength: Int
    /**
     * @return the request timestamp
     */
    val requestTimestamp: Long

    /**
     * Factory for {@link TopicCreationRequestEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link TopicCreationRequestEvent}
         */
        fun create(path: String, topicType: TopicType, initialValueLength: Int): TopicCreationRequestEvent {
            return TopicCreationRequestEventImpl(path, topicType, initialValueLength, System.currentTimeMillis())
        }

        /**
         * @return a new instance of {@link TopicCreationRequestEvent}
         */
        fun create(path: String, topicType: TopicType, initialValueLength: Int, timestamp: Long): TopicCreationRequestEvent {
            return TopicCreationRequestEventImpl(path, topicType, initialValueLength, timestamp)
        }
    }
}

/**
 * Implementation of {@link TopicCreationRequestEvent}.
 *
 * @author Matt Champion 17/05/2017
 */
private data class TopicCreationRequestEventImpl(
    override val path: String,
    override val topicType: TopicType,
    override val initialValueLength: Int,
    override val requestTimestamp: Long): TopicCreationRequestEvent

/**
 * Event describing a successful topic creation.
 *
 * @author Matt Champion 17/05/2017
 */
interface TopicCreationSuccessEvent {
    /**
     * @return the topic creation request event
     */
    val requestEvent: TopicCreationRequestEvent
    /**
     * @return the success timestamp
     */
    val successTimestamp: Long
    /**
     * @return the request timestamp
     */
    val requestTime: Long

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
 * @author Matt Champion 17/05/2017
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
 * @author Matt Champion 17/05/2017
 */
interface TopicCreationFailedEvent {
    /**
     * @return the topic creation request event
     */
    val requestEvent: TopicCreationRequestEvent
    /**
     * @return the failure reason
     */
    val failReason: TopicAddFailReason
    /**
     * @return the failure timestamp
     */
    val failedTimestamp: Long
    /**
     * @return the time to failure
     */
    val requestTime: Long

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
 * @author Matt Champion 17/05/2017
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
 * @author Matt Champion 17/05/2017
 */
interface PollRequestEvent {
    /**
     * @return the URI
     */
    val uri: String
    /**
     * @return the request timestamp
     */
    val requestTimestamp: Long

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
 * @author Matt Champion 17/05/2017
 */
private data class PollRequestEventImpl(
    override val uri: String,
    override val requestTimestamp: Long) : PollRequestEvent

/**
 * Event describing a successful poll request.
 *
 * @author Matt Champion 17/05/2017
 */
interface PollSuccessEvent {
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
     * @return the success timestamp
     */
    val successTimestamp: Long
    /**
     * @return the request timestamp
     */
    val requestTime: Long

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
 * @author Matt Champion 17/05/2017
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
 * @author Matt Champion 17/05/2017
 */
interface PollFailedEvent {
    /**
     * @return the poll request event
     */
    val requestEvent: PollRequestEvent
    /**
     * @return the exception
     */
    val exception: Exception
    /**
     * @return the failure timestamp
     */
    val failedTimestamp: Long
    /**
     * @return the time to failure
     */
    val requestTime: Long
        get() = failedTimestamp - requestEvent.requestTimestamp

    /**
     * Factory for {@link PollFailedEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link PollFailedEvent}
         */
        fun create(requestEvent: PollRequestEvent, exception: Exception): PollFailedEvent {
            return PollFailedEventImpl(requestEvent, exception, System.currentTimeMillis())
        }

        /**
         * @return a new instance of {@link PollFailedEvent}
         */
        fun create(requestEvent: PollRequestEvent, exception: Exception, timestamp: Long): PollFailedEvent {
            return PollFailedEventImpl(requestEvent, exception, timestamp)
        }
    }
}

/**
 * Event describing a failed poll.
 *
 * @author Matt Champion 17/05/2017
 */
private data class PollFailedEventImpl(
        override val requestEvent: PollRequestEvent,
        override val exception: Exception,
        override val failedTimestamp: Long) : PollFailedEvent {

    override val requestTime: Long
        get() = failedTimestamp - requestEvent.requestTimestamp
}

/**
 * Event describing a publication request.
 *
 * @author Matt Champion 17/05/2017
 */
interface PublicationRequestEvent {
    /**
     * @return the topic path
     */
    val path: String
    /**
     * @return the length of the value
     */
    val valueLength: Int
    /**
     * @return the request timestamp
     */
    val requestTimestamp: Long

    /**
     * Factory for {@link PublicationRequestEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link PublicationRequestEvent}
         */
        fun create(path: String, valueLength: Int): PublicationRequestEvent {
            return PublicationRequestEventImpl(path, valueLength, System.currentTimeMillis())
        }

        /**
         * @return a new instance of {@link PublicationRequestEvent}
         */
        fun create(path: String, valueLength: Int, timestamp: Long): PublicationRequestEvent {
            return PublicationRequestEventImpl(path, valueLength, timestamp)
        }
    }
}

/**
 * Event describing a publication request.
 *
 * @author Matt Champion 17/05/2017
 */
private data class PublicationRequestEventImpl(
    /**
     * @return the topic path
     */
    override val path: String,
    /**
     * @return the length of the value
     */
    override val valueLength: Int,
    /**
     * @return the request timestamp
     */
    override val requestTimestamp: Long) : PublicationRequestEvent

/**
 * Event describing a successful publication.
 *
 * @author Matt Champion 17/05/2017
 */
interface PublicationSuccessEvent {
    /**
     * @return the publication request event
     */
    val requestEvent: PublicationRequestEvent
    /**
     * @return the success timestamp
     */
    val successTimestamp: Long
    /**
     * @return the request timestamp
     */
    val requestTime: Long

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
 * @author Matt Champion 17/05/2017
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
 * @author Matt Champion 17/05/2017
 */
interface PublicationFailedEvent {
    /**
     * @return the publication request event
     */
    val requestEvent: PublicationRequestEvent
    /**
     * @return the error reason
     */
    val errorReason: ErrorReason
    /**
     * @return the failure timestamp
     */
    val failedTimestamp: Long
    /**
     * @return the time to failure
     */
    val requestTime: Long
        get() = failedTimestamp - requestEvent.requestTimestamp

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
 * @author Matt Champion 17/05/2017
 */
private data class PublicationFailedEventImpl(
        override val requestEvent: PublicationRequestEvent,
        override val errorReason: ErrorReason,
        override val failedTimestamp: Long) : PublicationFailedEvent {

    override val requestTime: Long
        get() = failedTimestamp - requestEvent.requestTimestamp
}
