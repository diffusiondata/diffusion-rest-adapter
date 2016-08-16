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

package com.pushtechnology.adapters.rest.model.store;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.persistence.Persistence;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * A model store that polls a persisted model to see if there has been a change.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class PollingPersistedModelStore extends AbstractModelStore {
    private static final Logger LOG = LoggerFactory.getLogger(PollingPersistedModelStore.class);

    private final Persistence persistence;
    private final ScheduledExecutorService executor;
    private final long period;
    private volatile Model latestModel;
    @GuardedBy("this")
    private Future<?> future;

    /**
     * Constructor.
     */
    public PollingPersistedModelStore(Persistence persistence, ScheduledExecutorService executor, long period) {
        this.persistence = persistence;
        this.executor = executor;
        this.period = period;
    }

    /**
     * Start polling.
     *
     * @throws IOException if there is a problem loading the model
     * @throws IllegalStateException if there is no initial model
     */
    public synchronized void start() throws IOException {
        stop();

        // Initialise the model on start
        final Optional<Model> model = persistence.loadModel();
        latestModel = model.orElseThrow(() -> new IllegalStateException("No model found on startup"));
        notifyListeners(latestModel);

        future = executor.scheduleAtFixedRate(new Poll(), period, period, TimeUnit.MILLISECONDS);
    }

    /**
     * Stop polling.
     */
    public synchronized void stop() {
        final Future<?> currentFuture = future;
        if (currentFuture == null) {
            return;
        }

        currentFuture.cancel(false);
    }

    @Override
    public Model get() {
        return latestModel;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(persistence=" + persistence + ", period=" + period + ")";
    }

    private final class Poll implements Runnable {
        @Override
        public void run() {
            final Optional<Model> model;
            try {
                model = persistence.loadModel();
            }
            catch (IOException e) {
                LOG.warn("Failed to load model when polling persistence", e);
                return;
            }

            final Model oldModel = latestModel;
            if (model.isPresent()) {
                final Model newModel = model.get();

                if (newModel.equals(oldModel)) {
                    return;
                }

                latestModel = newModel;
                notifyListeners(newModel);
            }
        }
    }
}
