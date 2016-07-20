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

import static java.util.stream.Collectors.counting;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.component.Component;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.PollClient;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * The {@link Component} that wires together the different components.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class ClientComponent implements Component {
    private static final Logger LOG = LoggerFactory.getLogger(ClientComponent.class);

    private final PublicationComponentFactory publicationComponentFactory = new PublicationComponentFactory();

    @GuardedBy("this")
    private PublicationComponent publicationComponent = PublicationComponent.INACTIVE;
    @GuardedBy("this")
    private PollingComponent pollingComponent = PollingComponent.INACTIVE;
    @GuardedBy("this")
    private Model currentModel = null;

    /**
     * Reconfigure the component.
     */
    public synchronized void reconfigure(
            Model model,
            PollClient pollClient,
            RESTAdapterClientCloseHandle client) throws IOException {

        final DiffusionConfig diffusionConfig = model.getDiffusion();
        final List<ServiceConfig> services = model.getServices();

        // Check to see if the new configuration performs useful work
        if (diffusionConfig == null ||
            services == null ||
            services.size() == 0 ||
            services.stream().map(ServiceConfig::getEndpoints).flatMap(Collection::stream).collect(counting()) == 0L) {

            LOG.info("Switching to inactive components");

            pollingComponent.close();
            publicationComponent.close();

            publicationComponent = PublicationComponent.INACTIVE;
            pollingComponent = PollingComponent.INACTIVE;
            currentModel = model;
            return;
        }

        // Check if the Diffusion configuration has changed and if it has replace all child components
        if (currentModel == null ||
            !currentModel.getDiffusion().equals(diffusionConfig) ||
            publicationComponent == PublicationComponent.INACTIVE) {

            LOG.info("Switching the polling and publishing components");

            pollingComponent.close();
            publicationComponent.close();

            publicationComponent = publicationComponentFactory.create(model, client);
            pollingComponent = publicationComponent.createPolling(model, pollClient);
            currentModel = model;
            return;
        }

        // Check if the services configuration has changed and if it has replace just the polling component
        if (!currentModel.getServices().equals(services) || pollingComponent == PollingComponent.INACTIVE) {
            LOG.info("Switching the polling component");

            pollingComponent.close();

            pollingComponent = publicationComponent.createPolling(model, pollClient);
            currentModel = model;
        }
    }

    @Override
    public synchronized void close() throws IOException {
        pollingComponent.close();
        publicationComponent.close();
    }
}
