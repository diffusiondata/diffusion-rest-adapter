
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
}

/**
 * Implementation of {@link ITopicCreationRequestEvent}.
 *
 * @author Matt Champion 17/05/2017
 */
data class TopicCreationRequestEvent(
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
}

/**
 * Event describing a successful topic creation.
 *
 * @author Matt Champion 17/05/2017
 */
data class TopicCreationSuccessEvent(
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
}

/**
 * Event describing a failed topic creation.
 *
 * @author Matt Champion 17/05/2017
 */
data class TopicCreationFailedEvent(
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
}

/**
 * Event describing a poll request.
 *
 * @author Matt Champion 17/05/2017
 */
data class PollRequestEvent(
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
}

/**
 * Event describing a successful poll request.
 *
 * @author Matt Champion 17/05/2017
 */
data class PollSuccessEvent(
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
}

/**
 * Event describing a failed poll.
 *
 * @author Matt Champion 17/05/2017
 */
data class PollFailedEvent(
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
}

/**
 * Event describing a publication request.
 *
 * @author Matt Champion 17/05/2017
 */
data class PublicationRequestEvent(
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
}

/**
 * Event describing a successful publication.
 *
 * @author Matt Champion 17/05/2017
 */
data class PublicationSuccessEvent(
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
}

/**
 * Event describing a failed publication.
 *
 * @author Matt Champion 17/05/2017
 */
data class PublicationFailedEvent(
    override val requestEvent: IPublicationRequestEvent,
    override val errorReason: ErrorReason,
    override val failedTimestamp: Long) : IPublicationFailedEvent {

    override val requestTime: Long
        get() = failedTimestamp - requestEvent.requestTimestamp
}
