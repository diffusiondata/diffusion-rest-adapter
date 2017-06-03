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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.pushtechnology.adapters.rest.metrics.listeners.PollListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * The metrics manager.
 * <p>
 * Provides the interface required by other components to
 * report metrics. It delegates the behaviour to other
 * components.
 *
 * @author Push Technology Limited
 */
public final class MetricsProvider implements
        PollListener,
        PublicationListener,
        TopicCreationListener,
        AutoCloseable {

    private final Runnable startTask;
    private final Runnable stopTask;
    private final PollListener pollListener;
    private final PublicationListener publicationListener;
    private final TopicCreationListener topicCreationListener;

    /**
     * Constructor.
     */
    public MetricsProvider(
            Runnable startTask,
            Runnable stopTask,
            PollListener pollListener,
            PublicationListener publicationListener,
            TopicCreationListener topicCreationListener) {
        this.startTask = startTask;
        this.stopTask = stopTask;
        this.pollListener = pollListener;
        this.publicationListener = publicationListener;
        this.topicCreationListener = topicCreationListener;
    }

    /**
     * Start logging the metrics.
     */
    @PostConstruct
    public void start() {
        startTask.run();
    }

    @PreDestroy
    @Override
    public void close() {
        stopTask.run();
    }

    @Override
    public PollCompletionListener onPollRequest(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        return pollListener.onPollRequest(serviceConfig, endpointConfig);
    }

    @Override
    public TopicCreationCompletionListener onTopicCreationRequest(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig) {
        return topicCreationListener.onTopicCreationRequest(serviceConfig, endpointConfig);
    }

    @Override
    public TopicCreationCompletionListener onTopicCreationRequest(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            Bytes value) {
        return topicCreationListener.onTopicCreationRequest(serviceConfig, endpointConfig, value);
    }

    @Override
    public PublicationCompletionListener onPublicationRequest(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            Bytes value) {
        return publicationListener.onPublicationRequest(serviceConfig, endpointConfig, value);
    }
}
