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
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.SSLContext;

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

    private static final SSLContextFactory SSL_CONTEXT_FACTORY = new SSLContextFactory();
    private static final HttpComponentFactory HTTP_COMPONENT_FACTORY = new HttpComponentFactory();

    private final PublicationComponentFactory publicationComponentFactory;

    @GuardedBy("this")
    private SSLContext sslContext = null;
    @GuardedBy("this")
    private HttpComponent httpComponent = HttpComponent.INACTIVE;
    @GuardedBy("this")
    private PublicationComponent publicationComponent = PublicationComponent.INACTIVE;
    @GuardedBy("this")
    private PollingComponent pollingComponent = PollingComponent.INACTIVE;
    @GuardedBy("this")
    private Model currentModel = null;

    /**
     * Constructor.
     */
    public ClientComponent(ScheduledExecutorService executor) {
        publicationComponentFactory = new PublicationComponentFactory(new PollingComponentFactory(executor));
    }

    /**
     * Reconfigure the component.
     */
    public synchronized void reconfigure(
            Model model,
            RESTAdapterClientCloseHandle client) throws IOException {

        sslContext = SSL_CONTEXT_FACTORY.create(model);

        if (isFirstConfiguration()) {
            initialConfiguration(model, client);
        }
        else if (isModelInactive(model)) {
            switchToInactiveComponents(model);
        }
        else if (hasDiffusionChanged(model)) {
            reconfigurePollingAndPublishing(model, client);
        }
        else if (haveServicesChanged(model)) {
            reconfigurePolling(model);
        }
    }

    private void initialConfiguration(Model model, RESTAdapterClientCloseHandle client) throws IOException {
        LOG.info("Setting up components for the first time");

        if (isModelInactive(model)) {
            currentModel = model;
        }
        else {
            httpComponent = HTTP_COMPONENT_FACTORY.create(sslContext);
            publicationComponent = publicationComponentFactory.create(model, client, sslContext);
            pollingComponent = publicationComponent.createPolling(model, httpComponent);
            currentModel = model;
        }
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

        publicationComponent = publicationComponentFactory.create(model, client, sslContext);
        pollingComponent = publicationComponent.createPolling(model, httpComponent);
        currentModel = model;
    }

    private void reconfigurePolling(Model model) {
        LOG.info("Replacing the polling component");

        pollingComponent.close();

        pollingComponent = publicationComponent.createPolling(model, httpComponent);
        currentModel = model;
    }

    private boolean isFirstConfiguration() {
        return currentModel == null;
    }

    private boolean isModelInactive(Model model) {
        final DiffusionConfig diffusionConfig = model.getDiffusion();
        final List<ServiceConfig> services = model.getServices();

        return diffusionConfig == null ||
            services == null ||
            services.size() == 0 ||
            services.stream().map(ServiceConfig::getEndpoints).flatMap(Collection::stream).collect(counting()) == 0L;
    }

    private boolean hasDiffusionChanged(Model model) {
        final DiffusionConfig diffusionConfig = model.getDiffusion();

        return currentModel.getDiffusion() == null ||
            !currentModel.getDiffusion().equals(diffusionConfig) ||
            publicationComponent == PublicationComponent.INACTIVE;
    }

    private boolean haveServicesChanged(Model model) {
        final List<ServiceConfig> newServices = model.getServices();
        final List<ServiceConfig> oldServices = model.getServices();

        return oldServices == null ||
            oldServices.equals(newServices) ||
            pollingComponent == PollingComponent.INACTIVE;
    }

    @Override
    public synchronized void close() throws IOException {
        pollingComponent.close();
        httpComponent.close();
        publicationComponent.close();
    }
}
