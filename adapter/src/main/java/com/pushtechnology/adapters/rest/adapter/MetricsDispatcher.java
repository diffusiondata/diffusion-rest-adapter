/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.adapter;

import java.util.ArrayList;
import java.util.Collection;

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.event.listeners.PollEventDispatcher;
import com.pushtechnology.adapters.rest.metrics.event.listeners.PollEventListener;
import com.pushtechnology.adapters.rest.metrics.event.listeners.PublicationEventDispatcher;
import com.pushtechnology.adapters.rest.metrics.event.listeners.PublicationEventListener;
import com.pushtechnology.adapters.rest.metrics.event.listeners.TopicCreationEventDispatcher;
import com.pushtechnology.adapters.rest.metrics.event.listeners.TopicCreationEventListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PollListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.topics.details.TopicType;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Dispatcher for metrics events.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class MetricsDispatcher implements
        PollListener,
        PublicationListener,
        TopicCreationListener {

    @GuardedBy("this")
    private final Collection<PollEventListener> pollListeners;
    @GuardedBy("this")
    private final Collection<PublicationEventListener> publicationListeners;
    @GuardedBy("this")
    private final Collection<TopicCreationEventListener> topicCreationListeners;

    private final PollEventDispatcher pollEventDispatcher;
    private final PublicationEventDispatcher publicationEventDispatcher;
    private final TopicCreationEventDispatcher topicCreationEventDispatcher;

    /**
     * Constructor.
     */
    public MetricsDispatcher() {
        pollListeners = new ArrayList<>();
        publicationListeners = new ArrayList<>();
        topicCreationListeners = new ArrayList<>();

        pollEventDispatcher = new PollEventDispatcher(new PollHandler());
        publicationEventDispatcher = new PublicationEventDispatcher(new PublicationHandler());
        topicCreationEventDispatcher = new TopicCreationEventDispatcher(new TopicCreationHandler());
    }

    /**
     * Add a poll event listener.
     */
    public synchronized void addPollEventListener(PollEventListener pollListener) {
        pollListeners.add(pollListener);
    }

    /**
     * Add a publication event listener.
     */
    public synchronized void addPublicationEventListener(PublicationEventListener publicationListener) {
        publicationListeners.add(publicationListener);
    }

    /**
     * Add a topic creation event listener.
     */
    public synchronized void addTopicCreationEventListener(TopicCreationEventListener topicCreationListener) {
        topicCreationListeners.add(topicCreationListener);
    }

    @Override
    public PollCompletionListener onPollRequest(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        return pollEventDispatcher.onPollRequest(serviceConfig, endpointConfig);
    }

    @Override
    public TopicCreationCompletionListener onTopicCreationRequest(String path, TopicType topicType) {
        return topicCreationEventDispatcher.onTopicCreationRequest(path, topicType);
    }

    @Override
    public PublicationCompletionListener onPublicationRequest(String path, int size) {
        return publicationEventDispatcher.onPublicationRequest(path, size);
    }

    private final class PollHandler implements PollEventListener {
        @Override
        public void onPollRequest(PollRequestEvent event) {
            synchronized (MetricsDispatcher.this) {
                pollListeners.forEach(listener -> listener.onPollRequest(event));
            }
        }

        @Override
        public void onPollSuccess(PollSuccessEvent event) {
            synchronized (MetricsDispatcher.this) {
                pollListeners.forEach(listener -> listener.onPollSuccess(event));
            }
        }

        @Override
        public void onPollFailed(PollFailedEvent event) {
            synchronized (MetricsDispatcher.this) {
                pollListeners.forEach(listener -> listener.onPollFailed(event));
            }
        }
    }

    private final class PublicationHandler implements PublicationEventListener {
        @Override
        public void onPublicationRequest(PublicationRequestEvent event) {
            synchronized (MetricsDispatcher.this) {
                publicationListeners.forEach(listener -> listener.onPublicationRequest(event));
            }
        }

        @Override
        public void onPublicationSuccess(PublicationSuccessEvent event) {
            publicationListeners.forEach(listener -> listener.onPublicationSuccess(event));
        }

        @Override
        public void onPublicationFailed(PublicationFailedEvent event) {
            publicationListeners.forEach(listener -> listener.onPublicationFailed(event));
        }
    }

    private final class TopicCreationHandler implements TopicCreationEventListener {
        @Override
        public void onTopicCreationRequest(TopicCreationRequestEvent event) {
            synchronized (MetricsDispatcher.this) {
                topicCreationListeners.forEach(listener -> listener.onTopicCreationRequest(event));
            }
        }

        @Override
        public void onTopicCreationSuccess(TopicCreationSuccessEvent event) {
            synchronized (MetricsDispatcher.this) {
                topicCreationListeners.forEach(listener -> listener.onTopicCreationSuccess(event));
            }
        }

        @Override
        public void onTopicCreationFailed(TopicCreationFailedEvent event) {
            synchronized (MetricsDispatcher.this) {
                topicCreationListeners.forEach(listener -> listener.onTopicCreationFailed(event));
            }
        }
    }
}
