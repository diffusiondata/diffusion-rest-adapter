/*******************************************************************************
 * Copyright (C) 2016 Push Technology Ltd.
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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.session.management.EventedSessionListener;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.ValueUpdater;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.datatype.json.JSON;

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
    private static final Logger LOG = LoggerFactory.getLogger(PublishingClientImpl.class);
    private final Session session;
    private final EventedSessionListener sessionListener;
    @GuardedBy("this")
    private Map<ServiceConfig, EventedUpdateSource> updaterSources = new HashMap<>();
    @GuardedBy("this")
    private Map<ServiceConfig, UpdaterSet> updaters = new HashMap<>();

    /**
     * Constructor.
     */
    public PublishingClientImpl(Session session, EventedSessionListener sessionListener) {
        this.session = session;
        this.sessionListener = sessionListener;
    }

    @Override
    public synchronized EventedUpdateSource addService(ServiceConfig serviceConfig) {

        final EventedUpdateSource source = new EventedUpdateSourceImpl(serviceConfig.getTopicRoot())
            .onActive(updater -> {
                synchronized (PublishingClientImpl.this) {
                    updaters.put(
                        serviceConfig,
                        new UpdaterSet(updater.valueUpdater(JSON.class), updater.valueUpdater(Binary.class)));
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
    @SuppressWarnings("unchecked")
    public <T> UpdateContext<T> createUpdateContext(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            Class<T> type) {

        final UpdaterSet updaterSet = updaters.get(serviceConfig);
        if (updaterSet == null) {
            throw new IllegalStateException("The service has not been added or is not active, no updater found");
        }

        final String topicPath = serviceConfig.getTopicRoot() + "/" + endpointConfig.getTopic();
        if (JSON.class.isAssignableFrom(type)) {
            final JSONUpdateContext jsonUpdateContext =
                new JSONUpdateContext(session, updaterSet.jsonUpdater, topicPath);
            sessionListener.onSessionStateChange(jsonUpdateContext);
            return (UpdateContext<T>) jsonUpdateContext;
        }
        else if (String.class.isAssignableFrom(type)) {
            final StringUpdateContext stringUpdateContext =
                new StringUpdateContext(session, updaterSet.binaryUpdater, topicPath);
            sessionListener.onSessionStateChange(stringUpdateContext);
            return (UpdateContext<T>) stringUpdateContext;
        }
        else if (Binary.class.isAssignableFrom(type)) {
            final BinaryUpdateContext binaryUpdateContext =
                new BinaryUpdateContext(session, updaterSet.binaryUpdater, topicPath);
            sessionListener.onSessionStateChange(binaryUpdateContext);
            return (UpdateContext<T>) binaryUpdateContext;
        }
        else {
            throw new IllegalArgumentException("Unsupported type");
        }
    }

    @Override
    public synchronized void publish(ServiceConfig serviceConfig, EndpointConfig endpointConfig, JSON json) {
        final UpdaterSet updaterSet = validatePublish(serviceConfig);
        if (updaterSet == null) {
            return;
        }

        final String topicName = serviceConfig.getTopicRoot() + "/" + endpointConfig.getTopic();
        updaterSet
            .jsonUpdater
            .update(
                topicName,
                json,
                topicName,
                UpdateTopicCallback.INSTANCE);
    }

    @Override
    public void publish(ServiceConfig serviceConfig, EndpointConfig endpointConfig, Binary binary) {
        final UpdaterSet updaterSet = validatePublish(serviceConfig);
        if (updaterSet == null) {
            return;
        }

        final String topicName = serviceConfig.getTopicRoot() + "/" + endpointConfig.getTopic();
        updaterSet
            .binaryUpdater
            .update(
                topicName,
                binary,
                topicName,
                UpdateTopicCallback.INSTANCE);
    }

    @Override
    public void publish(ServiceConfig serviceConfig, EndpointConfig endpointConfig, String value) {
        final UpdaterSet updaterSet = validatePublish(serviceConfig);
        if (updaterSet == null) {
            return;
        }

        final String topicName = serviceConfig.getTopicRoot() + "/" + endpointConfig.getTopic();
        updaterSet
            .binaryUpdater
            .update(
                topicName,
                Diffusion.dataTypes().binary().readValue(value.getBytes(Charset.forName("UTF-8"))),
                topicName,
                UpdateTopicCallback.INSTANCE);
    }

    private UpdaterSet validatePublish(ServiceConfig serviceConfig) {
        final Session.State state = session.getState();
        if (state.isClosed()) {
            throw new IllegalStateException("Session closed");
        }
        else if (state.isRecovering()) {
            LOG.debug("Dropped an update while session is recovering");
            return null;
        }

        final UpdaterSet updaterSet = updaters.get(serviceConfig);

        if (updaterSet == null) {
            LOG.debug("The service has not been added or is not active, no updater found");
            return null;
        }

        return updaterSet;
    }

    private static final class UpdaterSet {
        private final ValueUpdater<JSON> jsonUpdater;
        private final ValueUpdater<Binary> binaryUpdater;

        private UpdaterSet(ValueUpdater<JSON> jsonUpdater, ValueUpdater<Binary> binaryUpdater) {
            this.jsonUpdater = jsonUpdater;
            this.binaryUpdater = binaryUpdater;
        }
    }
}
