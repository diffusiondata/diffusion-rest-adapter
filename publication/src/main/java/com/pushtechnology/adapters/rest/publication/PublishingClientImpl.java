/*******************************************************************************
 * Copyright (C) 2019 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.publication;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.session.management.EventedSessionListener;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.DataType;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Implements {@link PublishingClient}.
 * <p>
 * Synchronises access to the session. Asynchronous operations may be outstanding when the session is closed.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class PublishingClientImpl implements PublishingClient {
    private final Session session;
    private final EventedSessionListener sessionListener;
    private final PublicationListener publicationListener;
    @GuardedBy("this")
    private Map<ServiceConfig, EventedUpdateSource> updaterSources = new HashMap<>();
    @GuardedBy("this")
    private Map<ServiceConfig, Updater> updaters = new HashMap<>();

    /**
     * Constructor.
     */
    public PublishingClientImpl(
            Session session,
            EventedSessionListener sessionListener,
            PublicationListener publicationListener) {
        this.session = session;
        this.sessionListener = sessionListener;
        this.publicationListener = publicationListener;
    }

    @Override
    public synchronized EventedUpdateSource addService(ServiceConfig serviceConfig) {

        final EventedUpdateSource source = new EventedUpdateSourceImpl(serviceConfig.getTopicPathRoot())
            .onActive(updater -> {
                synchronized (PublishingClientImpl.this) {
                    updaters.put(serviceConfig, updater);
                }
            })
            .onClose(() -> {
                synchronized (PublishingClientImpl.this) {
                    updaterSources.remove(serviceConfig);
                    updaters.remove(serviceConfig);
                }
            })
            .onError(errorReason -> {
                synchronized (PublishingClientImpl.this) {
                    updaterSources.remove(serviceConfig);
                    updaters.remove(serviceConfig);
                }
            });

        updaterSources.put(serviceConfig, source);
        source.register(session.feature(TopicUpdateControl.class));

        return source;
    }

    @Override
    public synchronized CompletableFuture<ServiceConfig> removeService(ServiceConfig serviceConfig) {
        final EventedUpdateSource registration = updaterSources.get(serviceConfig);

        if (registration != null) {
            final CompletableFuture<ServiceConfig> future = new CompletableFuture<>();
            registration
                .onClose(() -> future.complete(serviceConfig))
                .close();
            return future;
        }

        return CompletableFuture.completedFuture(serviceConfig);
    }

    @Override
    public <T> UpdateContext<T> createUpdateContext(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            Class<T> valueType,
            DataType<T> dataType) {

        final Updater updater = updaters.get(serviceConfig);
        if (updater == null) {
            throw new IllegalStateException("The service has not been added or is not active, no updater found");
        }

        final String topicPath = serviceConfig.getTopicPathRoot() + "/" + endpointConfig.getTopicPath();
        return createUpdateContext(topicPath, valueType, dataType, updater);
    }

    @Override
    public <T> UpdateContext<T> createUpdateContext(String path, Class<T> valueType, DataType<T> dataType) {
        return createUpdateContext(path, valueType, dataType, session.feature(TopicUpdateControl.class).updater());
    }

    private <T> UpdateContext<T> createUpdateContext(
            String path,
            Class<T> valueType,
            DataType<T> dataType,
            Updater updater) {
        final ValueUpdateContext<T> updateContext = new ValueUpdateContext<>(
            session,
            updater,
            path,
            valueType,
            dataType,
            publicationListener);
        sessionListener.onSessionStateChange(updateContext);
        return updateContext;
    }
}
