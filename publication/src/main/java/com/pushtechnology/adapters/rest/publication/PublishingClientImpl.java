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
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.UpdateSource;
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
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Session session;
    @GuardedBy("this")
    private Map<ServiceConfig, ValueUpdater<JSON>> updaters = new HashMap<>();

    /**
     * Constructor.
     */
    public PublishingClientImpl(Session session) {
        this.session = session;
    }

    @Override
    public synchronized void start() {
        isRunning.set(true);
    }

    @Override
    public synchronized void addService(ServiceConfig serviceConfig, ServiceReadyCallback readyCallback) {
        if (!isRunning.get()) {
            throw new IllegalStateException("Client has not started");
        }

        session
            .feature(TopicUpdateControl.class)
            .registerUpdateSource(
                serviceConfig.getTopicRoot(),
                new UpdateSource.Default() {
                    @Override
                    public void onActive(String topicPath, TopicUpdateControl.Updater updater) {
                        synchronized (PublishingClientImpl.this) {
                            LOG.warn("Active for service: {}", serviceConfig);
                            updaters.put(serviceConfig, updater.valueUpdater(JSON.class));
                            readyCallback.onServiceReady(serviceConfig);
                        }
                    }

                    @Override
                    public void onStandby(String topicPath) {
                        LOG.warn("On standby for service: {}", serviceConfig);
                    }
                });
    }

    @Override
    public synchronized void stop() {
        isRunning.set(false);
    }

    @Override
    public synchronized void publish(ServiceConfig serviceConfig, EndpointConfig endpointConfig, JSON json) {
        if (!isRunning.get()) {
            throw new IllegalStateException("Publishing client not started");
        }

        final Session.State state = session.getState();
        if (state.isClosed()) {
            throw new IllegalStateException("Session closed");
        }
        else if (state.isRecovering()) {
            LOG.debug("Dropped an update while session is recovering");
            return;
        }

        updaters.get(serviceConfig)
            .update(
                serviceConfig.getTopicRoot() + "/" + endpointConfig.getTopic(),
                json,
                serviceConfig.getTopicRoot() + "/" + endpointConfig.getTopic(),
                UpdateTopicCallback.INSTANCE);
    }
}
