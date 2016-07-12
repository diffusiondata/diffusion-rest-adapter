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

package com.pushtechnology.adapters;

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
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
    private final String host;
    private final int port;
    private final String principal;
    private final String password;
    @GuardedBy("this")
    private Session session;

    /**
     * Constructor.
     */
    public PublishingClientImpl(String host, int port, String principal, String password) {
        this.host = host;
        this.port = port;
        this.principal = principal;
        this.password = password;
    }

    @Override
    public synchronized void start() {
        // Use the session factory to open a new session
        SessionFactory sessionFactory = Diffusion
            .sessions()
            .listener(new Listener())
            .serverHost(host)
            .serverPort(port)
            .secureTransport(false)
            .transports(WEBSOCKET);

        if (principal != null && password != null) {
            sessionFactory = sessionFactory
                .principal(principal)
                .password(password);
        }

        session = sessionFactory
            .open();
    }

    @Override
    public synchronized void initialise(ServiceConfig serviceConfig) {
        final TopicControl topicControl = session.feature(TopicControl.class);

        serviceConfig
            .getEndpoints()
            .stream()
            .map(EndpointConfig::getTopic)
            .forEach(topicPath -> topicControl.addTopic(topicPath, JSON, topicPath, AddTopicCallback.INSTANCE));
    }

    @Override
    public synchronized void stop() {
        session.close();
    }

    @Override
    public synchronized void publish(EndpointConfig endpointConfig, JSON json) {
        if (!session.getState().isConnected()) {
            return;
        }

        session
            .feature(TopicUpdateControl.class)
            .updater()
            .valueUpdater(JSON.class)
            .update(endpointConfig.getTopic(), json, endpointConfig.getTopic(), UpdateTopicCallback.INSTANCE);
    }

    /**
     * A simple session state listener that logs out state changes.
     */
    private static final class Listener implements Session.Listener {

        @Override
        public void onSessionStateChanged(Session session, Session.State oldState, Session.State newState) {
            LOG.info("{} {} -> {}", session, oldState, newState);
        }
    }
}
