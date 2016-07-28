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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.ValueUpdater;
import com.pushtechnology.diffusion.client.session.Session;
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
    @GuardedBy("this")
    private Map<ServiceConfig, EventedUpdateSource> updaterSources = new HashMap<>();
    @GuardedBy("this")
    private Map<ServiceConfig, ValueUpdater<JSON>> updaters = new HashMap<>();

    /**
     * Constructor.
     */
    public PublishingClientImpl(Session session) {
        this.session = session;
    }

    @Override
    public synchronized EventedUpdateSource addService(ServiceConfig serviceConfig) {

        final EventedUpdateSource source = new EventedUpdateSourceImpl(serviceConfig.getTopicRoot())
            .onActive(updater -> {
                synchronized (PublishingClientImpl.this) {
                    LOG.warn("Active for service: {}", serviceConfig);
                    updaters.put(serviceConfig, updater.valueUpdater(JSON.class));
                }
            })
            .onStandby(() -> {
                LOG.warn("On standby for service: {}", serviceConfig);
            })
            .onClose(() -> {
                synchronized (PublishingClientImpl.this) {
                    LOG.warn("Closed for for service: {}", serviceConfig);
                    updaterSources.remove(serviceConfig);
                    updaters.remove(serviceConfig);
                }
            })
            .onError(errorReason -> {
                synchronized (PublishingClientImpl.this) {
                    LOG.warn("Closed for for service: {} because {}", serviceConfig, errorReason);
                    updaterSources.remove(serviceConfig);
                    updaters.remove(serviceConfig);
                }
            });

        updaterSources.put(serviceConfig, source);
        source.register(session.feature(TopicUpdateControl.class));

        return source;
    }

    @Override
    public synchronized void removeService(ServiceConfig serviceConfig) {
        final EventedUpdateSource registration = updaterSources.get(serviceConfig);

        if (registration != null) {
            registration.close();
        }
    }

    @Override
    public synchronized void publish(ServiceConfig serviceConfig, EndpointConfig endpointConfig, JSON json) {
        final Session.State state = session.getState();
        if (state.isClosed()) {
            throw new IllegalStateException("Session closed");
        }
        else if (state.isRecovering()) {
            LOG.debug("Dropped an update while session is recovering");
            return;
        }

        final ValueUpdater<JSON> updater = updaters.get(serviceConfig);

        if (updater == null) {
            LOG.debug("The service has not been added or is not active, no updater found");
            return;
        }

        updater
            .update(
                serviceConfig.getTopicRoot() + "/" + endpointConfig.getTopic(),
                json,
                serviceConfig.getTopicRoot() + "/" + endpointConfig.getTopic(),
                UpdateTopicCallback.INSTANCE);
    }
}
