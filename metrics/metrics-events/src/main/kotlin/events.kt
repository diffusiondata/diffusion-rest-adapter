
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
interface ITopicCreationRequestEvent {
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
     * Factory for {@link ITopicCreationRequestEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link ITopicCreationRequestEvent}
         */
        fun create(path: String, topicType: TopicType, initialValueLength: Int): ITopicCreationRequestEvent {
            return TopicCreationRequestEvent(path, topicType, initialValueLength, System.currentTimeMillis())
        }
    }
}

/**
 * Implementation of {@link ITopicCreationRequestEvent}.
 *
 * @author Matt Champion 17/05/2017
 */
private data class TopicCreationRequestEvent(
    override val path: String,
    override val topicType: TopicType,
    override val initialValueLength: Int,
    override val requestTimestamp: Long): ITopicCreationRequestEvent

/**
 * Event describing a successful topic creation.
 *
 * @author Matt Champion 17/05/2017
 */
interface ITopicCreationSuccessEvent {
    /**
     * @return the topic creation request event
     */
    val requestEvent: ITopicCreationRequestEvent
    /**
     * @return the success timestamp
     */
    val successTimestamp: Long
    /**
     * @return the request timestamp
     */
    val requestTime: Long

    /**
     * Factory for {@link ITopicCreationSuccessEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link ITopicCreationSuccessEvent}
         */
        fun create(requestEvent: ITopicCreationRequestEvent): ITopicCreationSuccessEvent {
            return TopicCreationSuccessEvent(requestEvent, System.currentTimeMillis())
        }
    }
}

/**
 * Event describing a successful topic creation.
 *
 * @author Matt Champion 17/05/2017
 */
private data class TopicCreationSuccessEvent(
    override val requestEvent: ITopicCreationRequestEvent,
    override val successTimestamp: Long) : ITopicCreationSuccessEvent {

    override val requestTime: Long
        get() = successTimestamp - requestEvent.requestTimestamp
}

/**
 * Event describing a failed topic creation.
 *
 * @author Matt Champion 17/05/2017
 */
interface ITopicCreationFailedEvent {
    /**
     * @return the topic creation request event
     */
    val requestEvent: ITopicCreationRequestEvent
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
     * Factory for {@link ITopicCreationFailedEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link ITopicCreationFailedEvent}
         */
        fun create(requestEvent: ITopicCreationRequestEvent, failReason: TopicAddFailReason): ITopicCreationFailedEvent {
            return TopicCreationFailedEvent(requestEvent, failReason, System.currentTimeMillis())
        }
    }
}

/**
 * Event describing a failed topic creation.
 *
 * @author Matt Champion 17/05/2017
 */
private data class TopicCreationFailedEvent(
    override val requestEvent: ITopicCreationRequestEvent,
    override val failReason: TopicAddFailReason,
    override val failedTimestamp: Long) : ITopicCreationFailedEvent {

    override val requestTime: Long
        get() = failedTimestamp - requestEvent.requestTimestamp
}

/**
 * Event describing a poll request.
 *
 * @author Matt Champion 17/05/2017
 */
interface IPollRequestEvent {
    /**
     * @return the URI
     */
    val uri: String
    /**
     * @return the request timestamp
     */
    val requestTimestamp: Long

    /**
     * Factory for {@link IPollRequestEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link IPollRequestEvent}
         */
        fun create(uri: String): IPollRequestEvent {
            return PollRequestEvent(uri, System.currentTimeMillis())
        }
    }
}

/**
 * Event describing a poll request.
 *
 * @author Matt Champion 17/05/2017
 */
private data class PollRequestEvent(
    override val uri: String,
    override val requestTimestamp: Long) : IPollRequestEvent

/**
 * Event describing a successful poll request.
 *
 * @author Matt Champion 17/05/2017
 */
interface IPollSuccessEvent {
    /**
     * @return the poll request event
     */
    val requestEvent: IPollRequestEvent
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
     * Factory for {@link IPollSuccessEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link IPollSuccessEvent}
         */
        fun create(requestEvent: IPollRequestEvent, statusCode: Int, responseLength: Long): IPollSuccessEvent {
            return PollSuccessEvent(requestEvent, statusCode, responseLength, System.currentTimeMillis())
        }
    }
}

/**
 * Event describing a successful poll request.
 *
 * @author Matt Champion 17/05/2017
 */
private data class PollSuccessEvent(
    override val requestEvent: IPollRequestEvent,
    override val statusCode: Int,
    override val responseLength: Long,
    override val successTimestamp: Long) : IPollSuccessEvent {

    override val requestTime: Long
        get() = successTimestamp - requestEvent.requestTimestamp
}

/**
 * Event describing a failed poll.
 *
 * @author Matt Champion 17/05/2017
 */
interface IPollFailedEvent {
    /**
     * @return the poll request event
     */
    val requestEvent: IPollRequestEvent
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
     * Factory for {@link IPollFailedEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link IPollFailedEvent}
         */
        fun create(requestEvent: IPollRequestEvent, exception: Exception): IPollFailedEvent {
            return PollFailedEvent(requestEvent, exception, System.currentTimeMillis())
        }
    }
}

/**
 * Event describing a failed poll.
 *
 * @author Matt Champion 17/05/2017
 */
private data class PollFailedEvent(
    override val requestEvent: IPollRequestEvent,
    override val exception: Exception,
    override val failedTimestamp: Long) : IPollFailedEvent {

    override val requestTime: Long
        get() = failedTimestamp - requestEvent.requestTimestamp
}

/**
 * Event describing a publication request.
 *
 * @author Matt Champion 17/05/2017
 */
interface IPublicationRequestEvent {
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
     * Factory for {@link IPublicationRequestEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link IPublicationRequestEvent}
         */
        fun create(path: String, valueLength: Int): IPublicationRequestEvent {
            return PublicationRequestEvent(path, valueLength, System.currentTimeMillis())
        }
    }
}

/**
 * Event describing a publication request.
 *
 * @author Matt Champion 17/05/2017
 */
private data class PublicationRequestEvent(
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
    override val requestTimestamp: Long) : IPublicationRequestEvent

/**
 * Event describing a successful publication.
 *
 * @author Matt Champion 17/05/2017
 */
interface IPublicationSuccessEvent {
    /**
     * @return the publication request event
     */
    val requestEvent: IPublicationRequestEvent
    /**
     * @return the success timestamp
     */
    val successTimestamp: Long
    /**
     * @return the request timestamp
     */
    val requestTime: Long

    /**
     * Factory for {@link IPublicationSuccessEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link IPublicationSuccessEvent}
         */
        fun create(requestEvent: IPublicationRequestEvent): IPublicationSuccessEvent {
            return PublicationSuccessEvent(requestEvent, System.currentTimeMillis())
        }
    }
}

/**
 * Event describing a successful publication.
 *
 * @author Matt Champion 17/05/2017
 */
private data class PublicationSuccessEvent(
    override val requestEvent: IPublicationRequestEvent,
    override val successTimestamp: Long) : IPublicationSuccessEvent {

    override val requestTime: Long
        get() = successTimestamp - requestEvent.requestTimestamp
}

/**
 * Event describing a failed publication.
 *
 * @author Matt Champion 17/05/2017
 */
interface IPublicationFailedEvent {
    /**
     * @return the publication request event
     */
    val requestEvent: IPublicationRequestEvent
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
     * Factory for {@link IPublicationFailedEvent}.
     */
    companion object Factory {
        /**
         * @return a new instance of {@link IPublicationFailedEvent}
         */
        fun create(requestEvent: IPublicationRequestEvent, errorReason: ErrorReason): IPublicationFailedEvent {
            return PublicationFailedEvent(requestEvent, errorReason, System.currentTimeMillis())
        }
    }
}

/**
 * Event describing a failed publication.
 *
 * @author Matt Champion 17/05/2017
 */
private data class PublicationFailedEvent(
    override val requestEvent: IPublicationRequestEvent,
    override val errorReason: ErrorReason,
    override val failedTimestamp: Long) : IPublicationFailedEvent {

    override val requestTime: Long
        get() = failedTimestamp - requestEvent.requestTimestamp
}
