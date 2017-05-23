/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

import static com.pushtechnology.adapters.rest.model.latest.MetricsConfig.Type.OFF;
import static java.util.Collections.emptyList;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.MetricsConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.store.AsyncMutableModelStore;
import com.pushtechnology.adapters.rest.model.store.ModelStore;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl;
import com.pushtechnology.diffusion.client.session.Session;

/**
 * A {@link ModelStore} implementation that is controlled by Diffusion messages.
 *
 * @author Push Technology Limited
 */
public final class ClientControlledModelStore implements ModelStore {
    /**
     * Path the model store registers to receive messages on.
     */
    /*package*/ static final String CONTROL_PATH = "adapter/rest/model/store";
    private final AsyncMutableModelStore delegateModelStore;

    /**
     * Constructor.
     */
    public ClientControlledModelStore(
        ScheduledExecutorService executor,
        DiffusionConfig diffusionConfig) {

        this(executor, diffusionConfig, null);
    }

    /**
     * Constructor.
     */
    public ClientControlledModelStore(
            ScheduledExecutorService executor,
            DiffusionConfig diffusionConfig,
            String truststore) {
        delegateModelStore = new AsyncMutableModelStore(executor)
            .setModel(Model
            .builder()
            .active(true)
            .diffusion(diffusionConfig)
            .services(emptyList())
            .truststore(truststore)
            .metrics(MetricsConfig
                .builder()
                .type(OFF)
                .build())
            .build());
    }

    /**
     * Notify the model store of the session to use.
     */
    public synchronized void onSessionReady(Session session) {
        final ModelController modelController = new ModelController(delegateModelStore);

        final RequestManager requestManager = new RequestManager(session.feature(MessagingControl.class));
        requestManager.addHandler(CONTROL_PATH, modelController);
    }

    @Override
    public synchronized Model get() {
        return delegateModelStore.get();
    }

    @Override
    public void onModelChange(Consumer<Model> listener) {
        delegateModelStore.onModelChange(listener);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
