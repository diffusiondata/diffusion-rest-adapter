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

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import net.jcip.annotations.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.session.Session;

/**
 * The REST Adapter.
 *
 * @author Matt Champion 07/10/2017
 */
public final class InternalRESTAdapter implements RESTAdapterListener {
    private static final Logger LOG = LoggerFactory.getLogger(InternalRESTAdapter.class);

    private final ContainerFactory containerFactory;

    @GuardedBy("this")
    private Components components;
    @GuardedBy("this")
    private Model currentModel;

    /**
     * Constructor.
     */
    public InternalRESTAdapter(ContainerFactory containerFactory) {
        this.containerFactory = containerFactory;
    }

    @Override
    public void onReconfiguration(Model model) {
        LOG.warn("Model {}", model);

        if (!model.isActive()) {
            // TODO
            return;
        }

        if (isFirstConfiguration()) {
            components = Components.create(containerFactory, model);
            currentModel = model;
            return;
        }

        currentModel = model;

        if (isModelInactive(model)) {
            components = components.switchToInactiveComponents();
        }
        else if (wasInactive() || hasTruststoreChanged(model)) {
            components = components.reconfigureAll(containerFactory, model);
        }
        else if (hasDiffusionChanged(model)) {
            components = components.reconfigureDiffusionSession(containerFactory, model);
        }
        else if (hasServiceSecurityChanged(model)) {
            components = components.reconfigureHTTPClient(containerFactory, model);
        }
        else if (haveServicesChanged(model)) {
            components = components.reconfigureServices(containerFactory, model);
        }
    }

    @Override
    public void onSessionOpen(Session session) {
        components = components.reconfigureHTTPClient(containerFactory, currentModel);
        components = components.reconfigureServices(containerFactory, currentModel);
    }

    @Override
    public void onSessionLost(Session session) {
        components = components.switchToInactiveComponents();
    }

    @Override
    public void onSessionClosed(Session session) {
    }

    @GuardedBy("this")
    private boolean isFirstConfiguration() {
        return currentModel == null;
    }

    @GuardedBy("this")
    private boolean isModelInactive(Model model) {
        final DiffusionConfig diffusionConfig = model.getDiffusion();
        final List<ServiceConfig> services = model.getServices();

        return diffusionConfig == null ||
            services == null ||
            services.size() == 0 ||
            services.stream().map(ServiceConfig::getEndpoints).mapToInt(Collection::size).sum() == 0;
    }

    @GuardedBy("this")
    private boolean wasInactive() {
        return !components.isActive();
    }

    @GuardedBy("this")
    private boolean hasTruststoreChanged(Model newModel) {
        return currentModel.getTruststore() == null && newModel.getTruststore() != null ||
            currentModel.getTruststore() != null && !currentModel.getTruststore().equals(newModel.getTruststore());
    }

    private boolean hasServiceSecurityChanged(Model newModel) {
        return !currentModel
            .getServices()
            .stream()
            .map(ServiceConfig::getSecurity)
            .filter(Objects::nonNull)
            .filter(securityConfig -> securityConfig.getBasic() != null)
            .collect(toList())
            .equals(newModel
                        .getServices()
                        .stream()
                        .map(ServiceConfig::getSecurity)
                        .filter(Objects::nonNull)
                        .filter(securityConfig -> securityConfig.getBasic() != null)
                        .collect(toList()));
    }

    @GuardedBy("this")
    private boolean hasDiffusionChanged(Model model) {
        final DiffusionConfig diffusionConfig = model.getDiffusion();

        return !currentModel.getDiffusion().equals(diffusionConfig);
    }

    private boolean haveServicesChanged(Model model) {
        final List<ServiceConfig> newServices = model.getServices();
        final List<ServiceConfig> oldServices = currentModel.getServices();

        return !oldServices.equals(newServices);
    }
}
