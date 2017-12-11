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

package com.pushtechnology.adapters.rest.metrics.event.listeners;

import com.pushtechnology.adapters.rest.endpoints.EndpointType;
import com.pushtechnology.adapters.rest.metrics.TopicCreationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;

/**
 * Implementation of {@link TopicCreationListener} that notifies a
 * {@link TopicCreationEventListener} of events.
 *
 * @author Push Technology Limited
 */
public final class TopicCreationEventDispatcher implements TopicCreationListener {
    private final TopicCreationEventListener listener;

    /**
     * Constructor.
     */
    public TopicCreationEventDispatcher(TopicCreationEventListener listener) {
        this.listener = listener;
    }

    @Override
    public TopicCreationCompletionListener onTopicCreationRequest(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig) {

        final TopicCreationRequestEvent requestEvent = TopicCreationRequestEvent.Factory.create(
            serviceConfig.getTopicPathRoot() + "/" + endpointConfig.getTopicPath(),
            EndpointType.from(endpointConfig.getProduces()).getTopicType());

        listener.onTopicCreationRequest(requestEvent);

        return new CompletionListener(listener, requestEvent);
    }

    /**
     * Implementation of {@link TopicCreationCompletionListener} that notifies a
     * {@link TopicCreationEventListener} of events.
     */
    private static final class CompletionListener implements TopicCreationCompletionListener {
        private final TopicCreationEventListener listener;
        private final TopicCreationRequestEvent requestEvent;

        /**
         * Constructor.
         */
        public CompletionListener(TopicCreationEventListener listener, TopicCreationRequestEvent requestEvent) {
            this.listener = listener;
            this.requestEvent = requestEvent;
        }

        @Override
        public void onTopicCreated() {
            listener.onTopicCreationSuccess(TopicCreationSuccessEvent.Factory.create(requestEvent));
        }

        @Override
        public void onTopicCreationFailed(TopicAddFailReason reason) {
            listener.onTopicCreationFailed(TopicCreationFailedEvent.Factory.create(requestEvent, reason));
        }
    }
}
