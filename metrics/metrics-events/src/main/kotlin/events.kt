
package com.pushtechnology.adapters.rest.metrics;

import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason
import com.pushtechnology.diffusion.client.topics.details.TopicType

/**
 * Event describing a topic creation request.
 *
 * @author Matt Champion 17/05/2017
 */
data class TopicCreationRequestEvent(
    /**
     * @return the topic path
     */
    val path: String,
    /**
     * @return the topic type
     */
    val topicType: TopicType,
    /**
     * @return the length of any initial value
     */
    val initialValueLength: Int,
    /**
     * @return the request timestamp
     */
    val requestTimestamp: Long)

/**
 * Event describing a successful topic creation.
 *
 * @author Matt Champion 17/05/2017
 */
data class TopicCreationSuccessEvent(
    /**
     * @return the topic creation request event
     */
    val requestEvent: TopicCreationRequestEvent,
    /**
     * @return the success timestamp
     */
    val successTimestamp: Long) {

    /**
     * @return the request timestamp
     */
    val requestTime: Long
        get() = successTimestamp - requestEvent.requestTimestamp
}

/**
 * Event describing a failed topic creation.
 *
 * @author Matt Champion 17/05/2017
 */
data class TopicCreationFailedEvent(
    /**
     * @return the topic creation request event
     */
    val requestEvent: TopicCreationRequestEvent,
    /**
     * @return the failure reason
     */
    val failReason: TopicAddFailReason,
    /**
     * @return the failure timestamp
     */
    val failedTimestamp: Long) {

    /**
     * @return the time to failure
     */
    val requestTime: Long
        get() = failedTimestamp - requestEvent.requestTimestamp
}
