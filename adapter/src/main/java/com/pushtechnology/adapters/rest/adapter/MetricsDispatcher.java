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

package com.pushtechnology.adapters.rest.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.HttpResponse;

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
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
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
    private final Collection<PollListener> pollListeners;
    @GuardedBy("this")
    private final Collection<PublicationListener> publicationListeners;
    @GuardedBy("this")
    private final Collection<TopicCreationListener> topicCreationListeners;

    /**
     * Constructor.
     */
    public MetricsDispatcher() {
        pollListeners = new ArrayList<>();
        publicationListeners = new ArrayList<>();
        topicCreationListeners = new ArrayList<>();
    }

    /**
     * Add a poll event listener.
     */
    public synchronized void addPollEventListener(PollEventListener pollListener) {
        pollListeners.add(new PollEventDispatcher(pollListener));
    }

    /**
     * Add a publication event listener.
     */
    public synchronized void addPublicationEventListener(PublicationEventListener publicationListener) {
        publicationListeners.add(new PublicationEventDispatcher(publicationListener));
    }

    /**
     * Add a topic creation event listener.
     */
    public synchronized void addTopicCreationEventListener(TopicCreationEventListener topicCreationListener) {
        topicCreationListeners.add(new TopicCreationEventDispatcher(topicCreationListener));
    }

    @Override
    public PollCompletionListener onPollRequest(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        final Collection<PollCompletionListener> listeners = new ArrayList<>();
        synchronized (this) {
            pollListeners.forEach(pollListener -> {
                final PollCompletionListener completionListener =
                    pollListener.onPollRequest(serviceConfig, endpointConfig);
                listeners.add(completionListener);
            });
        }
        return new PollCompletionListener() {
            @Override
            public void onPollResponse(HttpResponse response) {
                listeners.forEach(listener -> listener.onPollResponse(response));
            }

            @Override
            public void onPollFailure(Exception exception) {
                listeners.forEach(listener -> listener.onPollFailure(exception));
            }
        };
    }

    @Override
    public TopicCreationCompletionListener onTopicCreationRequest(String path, TopicType topicType) {

        final Collection<TopicCreationCompletionListener> listeners = new ArrayList<>();
        synchronized (this) {
            topicCreationListeners.forEach(topicCreationListener -> {
                final TopicCreationCompletionListener completionListener =
                    topicCreationListener.onTopicCreationRequest(path, topicType);
                listeners.add(completionListener);
            });
        }
        return new TopicCreationCompletionListener() {
            @Override
            public void onTopicCreated() {
                listeners.forEach(TopicCreationCompletionListener::onTopicCreated);
            }

            @Override
            public void onTopicCreationFailed(TopicAddFailReason reason) {
                listeners.forEach(listener -> listener.onTopicCreationFailed(reason));
            }
        };
    }

    @Override
    public PublicationCompletionListener onPublicationRequest(String path, int size) {

        final Collection<PublicationCompletionListener> listeners = new ArrayList<>();
        synchronized (this) {
            publicationListeners.forEach(publicationListener -> {
                final PublicationCompletionListener completionListener =
                    publicationListener.onPublicationRequest(path, size);
                listeners.add(completionListener);
            });
        }
        return new PublicationCompletionListener() {
            @Override
            public void onPublication() {
                listeners.forEach(PublicationCompletionListener::onPublication);
            }

            @Override
            public void onPublicationFailed(ErrorReason reason) {
                listeners.forEach(listener -> listener.onPublicationFailed(reason));
            }
        };
    }
}
