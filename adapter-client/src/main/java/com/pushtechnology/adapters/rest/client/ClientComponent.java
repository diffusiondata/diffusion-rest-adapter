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
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.component.Component;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.HttpComponent;
import com.pushtechnology.adapters.rest.polling.HttpComponentFactory;

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

    private static final HttpComponentFactory HTTP_COMPONENT_FACTORY = new HttpComponentFactory();
    private static final PublicationComponentFactory PUBLICATION_COMPONENT_FACTORY = new PublicationComponentFactory(
        new PollingComponentFactory(Executors::newSingleThreadScheduledExecutor));

    @GuardedBy("this")
    private HttpComponent httpComponent = HttpComponent.INACTIVE;
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
            RESTAdapterClientCloseHandle client) throws IOException {

        final DiffusionConfig diffusionConfig = model.getDiffusion();
        final List<ServiceConfig> services = model.getServices();

        if (currentModel == null) {
            initialConfiguration(model, client);
        }
        else if (diffusionConfig == null ||
            // Check to see if the new configuration performs useful work
            services == null ||
            services.size() == 0 ||
            services.stream().map(ServiceConfig::getEndpoints).flatMap(Collection::stream).collect(counting()) == 0L) {

            switchToInactiveComponents(model);
        }
        else if (currentModel.getDiffusion().equals(diffusionConfig) ||
                publicationComponent == PublicationComponent.INACTIVE) {

            reconfigurePollingAndPublishing(model, client);
        }
        else if (!currentModel.getServices().equals(services) || pollingComponent == PollingComponent.INACTIVE) {

            reconfigurePolling(model);
        }
    }

    private void initialConfiguration(Model model, RESTAdapterClientCloseHandle client) {
        LOG.info("Setting up components for the first time");

        httpComponent = HTTP_COMPONENT_FACTORY.create(model);
        httpComponent.start();
        publicationComponent = PUBLICATION_COMPONENT_FACTORY.create(model, client);
        pollingComponent = publicationComponent.createPolling(model, httpComponent);
        currentModel = model;
    }



    private void switchToInactiveComponents(Model model) throws IOException {
        LOG.info("Replacing with inactive components");

        pollingComponent.close();
        publicationComponent.close();

        publicationComponent = PublicationComponent.INACTIVE;
        pollingComponent = PollingComponent.INACTIVE;
        currentModel = model;
    }

    private void reconfigurePollingAndPublishing(Model model, RESTAdapterClientCloseHandle client) throws IOException {
        LOG.info("Replacing the polling and publishing components");

        pollingComponent.close();
        publicationComponent.close();

        publicationComponent = PUBLICATION_COMPONENT_FACTORY.create(model, client);
        pollingComponent = publicationComponent.createPolling(model, httpComponent);
        currentModel = model;
    }

    private void reconfigurePolling(Model model) {
        LOG.info("Replacing the polling component");

        pollingComponent.close();

        pollingComponent = publicationComponent.createPolling(model, httpComponent);
        currentModel = model;
    }

    @Override
    public synchronized void close() throws IOException {
        pollingComponent.close();
        httpComponent.close();
        publicationComponent.close();
    }
}
