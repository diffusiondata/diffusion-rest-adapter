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

package com.pushtechnology.adapters.rest.adapter;

import static java.util.stream.Collectors.counting;

import java.util.Collection;
import java.util.List;

import org.picocontainer.MutablePicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * The components of the REST adapter.
 *
 * @author Matt Champion 27/07/2017
 */
public final class Components {
    private static final Logger LOG = LoggerFactory.getLogger(Components.class);
    private final MutablePicoContainer topLevelContainer;
    private final MutablePicoContainer tlsContainer;
    private final MutablePicoContainer diffusionContainer;
    private final MutablePicoContainer httpContainer;
    private final MutablePicoContainer servicesContainer;
    private final ServiceManager serviceManager;

    private Components(
            MutablePicoContainer topLevelContainer,
            MutablePicoContainer tlsContainer,
            MutablePicoContainer diffusionContainer,
            MutablePicoContainer httpContainer,
            MutablePicoContainer servicesContainer,
            ServiceManager serviceManager) {

        this.topLevelContainer = topLevelContainer;
        this.tlsContainer = tlsContainer;
        this.diffusionContainer = diffusionContainer;
        this.httpContainer = httpContainer;
        this.servicesContainer = servicesContainer;
        this.serviceManager = serviceManager;
    }

    /*package*/ Components reconfigureAll(ContainerFactory containerFactory, Model model) {
        LOG.warn("Replacing all components");

        if (tlsContainer != null) {
            tlsContainer.dispose();
            topLevelContainer.removeChildContainer(tlsContainer);
        }

        final MutablePicoContainer newTlsContainer = containerFactory.newTLSContainer(model, topLevelContainer);
        final MutablePicoContainer newDiffusionContainer =
            containerFactory.newDiffusionContainer(model, newTlsContainer);
        final MutablePicoContainer newHttpContainer = containerFactory.newHttpContainer(model, newDiffusionContainer);
        final MutablePicoContainer newServicesContainer =
            containerFactory.newServicesContainer(model, newHttpContainer);

        final ServiceManagerContext managerContext = newServicesContainer.getComponent(ServiceManagerContext.class);

        newTlsContainer.start();

        serviceManager.reconfigure(managerContext, model);

        return new Components(
            topLevelContainer,
            newTlsContainer,
            newDiffusionContainer,
            newHttpContainer,
            newServicesContainer,
            serviceManager);
    }

    /*package*/ Components reconfigureDiffusionSession(ContainerFactory containerFactory, Model model) {
        LOG.warn("Replacing Diffusion session");

        if (diffusionContainer != null) {
            diffusionContainer.dispose();
            tlsContainer.removeChildContainer(diffusionContainer);
        }

        final MutablePicoContainer newDiffusionContainer = containerFactory.newDiffusionContainer(model, tlsContainer);
        final MutablePicoContainer newHttpContainer = containerFactory.newHttpContainer(model, newDiffusionContainer);
        final MutablePicoContainer newServicesContainer =
            containerFactory.newServicesContainer(model, newHttpContainer);

        final ServiceManagerContext managerContext = newServicesContainer.getComponent(ServiceManagerContext.class);

        newDiffusionContainer.start();

        serviceManager.reconfigure(managerContext, model);

        return new Components(
            topLevelContainer,
            tlsContainer,
            newDiffusionContainer,
            newHttpContainer,
            newServicesContainer,
            serviceManager);
    }

    /*package*/ Components reconfigureHTTPClient(ContainerFactory containerFactory, Model model) {
        LOG.warn("Replacing HTTP client");

        if (httpContainer != null) {
            httpContainer.dispose();
            diffusionContainer.removeChildContainer(httpContainer);
        }

        final MutablePicoContainer newHttpContainer = containerFactory.newHttpContainer(model, diffusionContainer);
        final MutablePicoContainer newServicesContainer =
            containerFactory.newServicesContainer(model, newHttpContainer);

        final ServiceManagerContext managerContext = newServicesContainer.getComponent(ServiceManagerContext.class);

        newHttpContainer.start();

        serviceManager.reconfigure(managerContext, model);

        return new Components(
            topLevelContainer,
            tlsContainer,
            diffusionContainer,
            newHttpContainer,
            newServicesContainer,
            serviceManager);
    }

    /*package*/ Components reconfigureServices(ContainerFactory containerFactory, Model model) {
        LOG.warn("Replacing HTTP sessions");

        if (servicesContainer != null) {
            servicesContainer.dispose();
            httpContainer.removeChildContainer(servicesContainer);
        }

        final MutablePicoContainer newServicesContainer = containerFactory.newServicesContainer(model, httpContainer);

        final ServiceManagerContext managerContext = newServicesContainer.getComponent(ServiceManagerContext.class);

        newServicesContainer.start();

        serviceManager.reconfigure(managerContext, model);

        return new Components(
            topLevelContainer,
            tlsContainer,
            diffusionContainer,
            httpContainer,
            newServicesContainer,
            serviceManager);
    }

    /*package*/ Components switchToInactiveComponents() {
        LOG.warn("Putting adapter to sleep");

        if (tlsContainer != null) {

            serviceManager.close();
            tlsContainer.dispose();

            return new Components(
                topLevelContainer,
                null,
                null,
                null,
                null,
                serviceManager);
        }
        else {
            return this;
        }
    }

    /*package*/ boolean isActive() {
        return tlsContainer != null;
    }

    /*package*/ void close() {
        if (tlsContainer != null) {
            tlsContainer.dispose();
        }

        serviceManager.close();
    }

    /*package*/ static Components create(ContainerFactory containerFactory, Model model) {
        if (!isModelInactive(model)) {
            final MutablePicoContainer topLevelContainer = containerFactory.newTopLevelContainer();
            final MutablePicoContainer tlsContainer = containerFactory.newTLSContainer(model, topLevelContainer);
            final MutablePicoContainer diffusionContainer = containerFactory.newDiffusionContainer(model, tlsContainer);
            final MutablePicoContainer httpContainer = containerFactory.newHttpContainer(model, diffusionContainer);
            final MutablePicoContainer servicesContainer = containerFactory.newServicesContainer(model, httpContainer);

            final ServiceManagerContext managerContext = servicesContainer.getComponent(ServiceManagerContext.class);
            final ServiceManager serviceManager = topLevelContainer.getComponent(ServiceManager.class);

            tlsContainer.start();

            serviceManager.reconfigure(managerContext, model);

            return new Components(
                topLevelContainer,
                tlsContainer,
                diffusionContainer,
                httpContainer,
                servicesContainer,
                serviceManager);
        }
        else {
            final MutablePicoContainer topLevelContainer = containerFactory.newTopLevelContainer();
            final ServiceManager serviceManager = topLevelContainer.getComponent(ServiceManager.class);

            return new Components(
                topLevelContainer,
                null,
                null,
                null,
                null,
                serviceManager);
        }
    }

    private static boolean isModelInactive(Model model) {
        final DiffusionConfig diffusionConfig = model.getDiffusion();
        final List<ServiceConfig> services = model.getServices();

        return diffusionConfig == null ||
            services == null ||
            services.size() == 0 ||
            services.stream().map(ServiceConfig::getEndpoints).flatMap(Collection::stream).collect(counting()) == 0L;
    }
}
