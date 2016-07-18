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

package com.pushtechnology.adapters.rest.client;

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.polling.HttpClientFactoryImpl;
import com.pushtechnology.adapters.rest.polling.PollClient;
import com.pushtechnology.adapters.rest.polling.PollClientImpl;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;

import net.jcip.annotations.ThreadSafe;

/**
 * Simple client adapting REST to Diffusion.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class RESTAdapterClient {
    private static final Logger LOG = LoggerFactory.getLogger(RESTAdapterClient.class);

    private final AtomicReference<RESTAdapterClientState> state = new AtomicReference<>(null);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Model model;
    private final PollClient pollClient;

    private RESTAdapterClient(Model model, PollClient pollClient) {
        this.model = model;
        this.pollClient = pollClient;
    }

    /**
     * Start the client.
     * @throws IllegalStateException if the client is running
     */
    public synchronized void start() {
        if (!isRunning.compareAndSet(false, true)) {
            throw new IllegalStateException("The client is already running");
        }

        final Session session = getSession(model.getDiffusion());
        pollClient.start();
        state.set(RESTAdapterClientState.create(model, pollClient, session));
    }

    /**
     * Stop the client.
     * @throws IllegalStateException if the client is not running
     */
    public synchronized void stop() throws IOException {
        if (!isRunning.compareAndSet(true, false)) {
            throw new IllegalStateException("The client is not running");
        }

        state.get().close();
        pollClient.stop();
    }

    /**
     * Factory method for {@link RESTAdapterClient}.
     * @param model the configuration to use
     * @return a new {@link RESTAdapterClient}
     */
    public static RESTAdapterClient create(Model model) {
        LOG.debug("Creating REST adapter client with configuration: {}", model);
        final PollClient pollClient = new PollClientImpl(new HttpClientFactoryImpl());

        return new RESTAdapterClient(model, pollClient);
    }

    private Session getSession(DiffusionConfig diffusionConfig) {
        final SessionFactory sessionFactory = Diffusion
            .sessions()
            .serverHost(diffusionConfig.getHost())
            .serverPort(diffusionConfig.getPort())
            .secureTransport(false)
            .transports(WEBSOCKET)
            .reconnectionTimeout(5000)
            .listener(new Listener());

        if (diffusionConfig.getPrincipal() != null && diffusionConfig.getPassword() != null) {
            return sessionFactory
                .principal(diffusionConfig.getPrincipal())
                .password(diffusionConfig.getPassword())
                .open();
        }
        else  {
            return sessionFactory.open();
        }
    }

    /**
     * A {@link Session.Listener} to handle session closes.
     * <p>
     * Ignores state transitions while the client is running. If the session closes when the client is running the
     * connection and recovery must have failed, stop the client.
     */
    private final class Listener implements Session.Listener {
        @Override
        public void onSessionStateChanged(Session forSession, Session.State oldState, Session.State newState) {
            synchronized (RESTAdapterClient.this) {
                if (!isRunning.get()) {
                    return;
                }

                LOG.warn("{} {} -> {}", forSession, oldState, newState);

                if (newState.isClosed()) {
                    try {
                        stop();
                    }
                    catch (IOException e) {
                        LOG.warn("Exception stopping client", e);
                    }
                }
            }
        }
    }
}
