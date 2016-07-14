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

import static com.pushtechnology.diffusion.client.topics.details.TopicType.JSON;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.UpdateSource;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.ValueUpdater;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;
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
    private final SessionFactory sessionFactory;
    @GuardedBy("this")
    private Session session;
    @GuardedBy("this")
    private Map<ServiceConfig, ValueUpdater<JSON>> updaters = new HashMap<>();

    /**
     * Constructor.
     */
    public PublishingClientImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public synchronized void start(Session.Listener listener) {
        session = sessionFactory
            .listener(listener)
            .open();
    }

    @Override
    public synchronized void initialise(ServiceConfig serviceConfig, InitialiseCallback callback) {
        if (session == null) {
            throw new IllegalStateException("Client has not started");
        }

        final TopicControl topicControl = session.feature(TopicControl.class);

        final TopicCreationInitialisationAdapter addCallback =
            new TopicCreationInitialisationAdapter(serviceConfig, new Initialise(callback));

        serviceConfig
            .getEndpoints()
            .stream()
            .forEach(endpoint -> topicControl.addTopic(
                serviceConfig.getTopicRoot() + "/" + endpoint.getTopic(),
                JSON,
                endpoint,
                addCallback));
    }

    @Override
    public synchronized void stop() {
        if (session == null) {
            return;
        }

        session.close();
        session = null;
    }

    @Override
    public synchronized void publish(ServiceConfig serviceConfig, EndpointConfig endpointConfig, JSON json) {
        if (session == null) {
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

    private final class Initialise implements InitialiseCallback {
        private final InitialiseCallback delegate;

        private Initialise(InitialiseCallback delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onEndpointAdded(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
            delegate.onEndpointAdded(serviceConfig, endpointConfig);
        }

        @Override
        public void onEndpointFailed(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
            delegate.onEndpointFailed(serviceConfig, endpointConfig);
        }

        @Override
        public void onServiceAdded(ServiceConfig serviceConfig) {
            synchronized (PublishingClientImpl.this) {
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
                                    delegate.onServiceAdded(serviceConfig);
                                }
                            }

                            @Override
                            public void onStandby(String topicPath) {
                                LOG.warn("On standby for service: {}", serviceConfig);
                            }
                        });
            }
        }
    }
}
