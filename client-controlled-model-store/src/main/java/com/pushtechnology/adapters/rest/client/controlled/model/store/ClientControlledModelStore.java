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

import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.store.AbstractModelStore;
import com.pushtechnology.adapters.rest.model.store.ModelStore;
import com.pushtechnology.adapters.rest.session.management.DiffusionSessionFactory;
import com.pushtechnology.adapters.rest.session.management.EventedSessionListener;
import com.pushtechnology.adapters.rest.session.management.SessionLostListener;
import com.pushtechnology.diffusion.client.session.Session;

/**
 * A {@link ModelStore} implementation that is controlled by Diffusion messages.
 *
 * @author Push Technology Limited
 */
public final class ClientControlledModelStore extends AbstractModelStore implements ModelStore, AutoCloseable {
    private final DiffusionConfig diffusionConfig;
    private final SSLContext sslContext;
    private Session session;
    private Model model;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Constructor.
     */
    /*package*/ ClientControlledModelStore(DiffusionConfig diffusionConfig, SSLContext sslContext) {
        this.diffusionConfig = diffusionConfig;
        this.sslContext = sslContext;
        this.model = Model
            .builder()
            .active(true)
            .diffusion(diffusionConfig)
            .services(emptyList())
            .build();
    }

    /**
     * Start the model store.
     */
    public void start() {
        start(DiffusionSessionFactory.create());
    }

    /*package*/ synchronized void start(DiffusionSessionFactory sessionFactory) {
        if (!isRunning.compareAndSet(false, true)) {
            throw new IllegalStateException("The " + this + " has already been started");
        }

        final EventedSessionListener listener = new EventedSessionListener();
        final SessionLostListener sessionLostListener = new SessionLostListener(() -> { });
        session = sessionFactory.openSession(diffusionConfig, sessionLostListener, listener, sslContext);
    }

    @Override
    public synchronized void close() {
        if (!isRunning.compareAndSet(true, false)) {
            throw new IllegalStateException("The " + this + " has not yet been started");
        }

        session.close();
    }

    @Override
    public synchronized Model get() {
        return model;
    }

    /**
     * @return a new model store
     */
    public static ModelStore create(DiffusionConfig diffusionConfig, SSLContext sslContext) {
        return new ClientControlledModelStore(diffusionConfig, sslContext);
    }
}
