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

import static com.pushtechnology.adapters.rest.component.Component.INACTIVE;
import static java.util.stream.Collectors.counting;

import java.util.Collection;
import java.util.List;

import com.pushtechnology.adapters.rest.component.Component;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.PollClient;

/**
 * Factory for snapshots of the {@link RESTAdapterClient} for a configuration model.
 * <P>
 * If either there is no Diffusion configuration or nothing to poll is configured the
 * inactive snapshot is returned. Otherwise an active snapshot is created.
 *
 * @author Push Technology Limited
 */
public final class RESTAdapterComponentFactoryImpl implements RESTAdapterComponentFactory {
    private final RESTAdapterComponentFactory activeSnapshotFactory;

    /**
     * Constructor.
     */
    /*package*/ RESTAdapterComponentFactoryImpl(RESTAdapterComponentFactory activeSnapshotFactory) {
        this.activeSnapshotFactory = activeSnapshotFactory;
    }

    @Override
    public Component create(
        Model model,
        PollClient pollClient,
        RESTAdapterClientCloseHandle client) {

        final DiffusionConfig diffusionConfig = model.getDiffusion();
        final List<ServiceConfig> services = model.getServices();

        if (diffusionConfig == null ||
            services == null ||
            services.size() == 0 ||
            services.stream().map(ServiceConfig::getEndpoints).flatMap(Collection::stream).collect(counting()) == 0L) {

            return INACTIVE;
        }

        return activeSnapshotFactory.create(model, pollClient, client);
    }
}
