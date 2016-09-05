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

package com.pushtechnology.adapters.rest.client.controlled.model.store;

import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.store.AsyncMutableModelStore;
import com.pushtechnology.adapters.rest.model.store.ModelStore;
import com.pushtechnology.adapters.rest.session.management.DiffusionSessionFactory;
import com.pushtechnology.adapters.rest.session.management.EventedSessionListener;
import com.pushtechnology.adapters.rest.session.management.SessionLostListener;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionEstablishmentException;

import net.jcip.annotations.GuardedBy;

/**
 * A {@link ModelStore} implementation that is controlled by Diffusion messages.
 *
 * @author Push Technology Limited
 */
public final class ClientControlledModelStore implements ModelStore, AutoCloseable {
    /**
     * Path the model store registers to receive messages on.
     */
    /*package*/ static final String CONTROL_PATH = "adapter/rest/model/store";
    // Replace the session when lost
    private final SessionLostListener sessionLostListener;
    private final DiffusionConfig diffusionConfig;
    private final SSLContext sslContext;
    private final DiffusionSessionFactory sessionFactory;
    private final AsyncMutableModelStore delegateModelStore;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Session session;

    /**
     * Constructor.
     */
    /*package*/ ClientControlledModelStore(
            ScheduledExecutorService executor,
            DiffusionConfig diffusionConfig,
            SSLContext sslContext,
            DiffusionSessionFactory sessionFactory) {
        this.diffusionConfig = diffusionConfig;
        this.sslContext = sslContext;
        this.sessionFactory = sessionFactory;
        sessionLostListener = new SessionLostListener(() -> executor.schedule(this::createSession, 5, SECONDS));
        delegateModelStore = new AsyncMutableModelStore(executor)
            .setModel(Model
            .builder()
            .active(true)
            .diffusion(diffusionConfig)
            .services(emptyList())
            .build());
    }

    /**
     * Start the model store.
     */
    public synchronized void start() {
        if (!isRunning.compareAndSet(false, true)) {
            throw new IllegalStateException("The " + this + " has already been started");
        }

        createSession();
    }

    @GuardedBy("this")
    private synchronized void createSession() {
        if (!isRunning.get()) {
            // Possible if closed while session is lost
            return;
        }

        final EventedSessionListener listener = new EventedSessionListener();
        try {
            session = sessionFactory.openSession(diffusionConfig, sessionLostListener, listener, sslContext);
        }
        catch (SessionEstablishmentException e) {
            // Handled by the session lost listener
            return;
        }

        final ModelPublisher modelPublisher = ModelPublisherImpl.create(session);

        modelPublisher.initialise(delegateModelStore.get());

        final ModelController modelController = new ModelController(delegateModelStore, modelPublisher);

        final RequestManager requestManager = new RequestManager(session.feature(MessagingControl.class));
        requestManager.addHandler(CONTROL_PATH, modelController);
    }

    @Override
    public synchronized void close() {
        if (!isRunning.compareAndSet(true, false)) {
            throw new IllegalStateException("The " + this + " has not yet been started");
        }

        if (session != null) {
            // Possible if closed while initial session failed
            session.close();
        }
    }

    @Override
    public synchronized Model get() {
        return delegateModelStore.get();
    }

    @Override
    public void onModelChange(Consumer<Model> listener) {
        delegateModelStore.onModelChange(listener);
    }

    /**
     * @return a new model store
     */
    public static ClientControlledModelStore create(
            ScheduledExecutorService executor,
            DiffusionConfig diffusionConfig,
            SSLContext sslContext) {
        return new ClientControlledModelStore(executor, diffusionConfig, sslContext, DiffusionSessionFactory.create());
    }

}
