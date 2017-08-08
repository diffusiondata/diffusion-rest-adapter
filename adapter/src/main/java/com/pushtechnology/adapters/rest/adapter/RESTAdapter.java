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
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.session.management.SessionLossHandler;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * The REST Adapter.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class RESTAdapter implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(RESTAdapter.class);

    private final ContainerFactory containerFactory;
    private final Runnable shutdownTask;

    @GuardedBy("this")
    private Components components;
    @GuardedBy("this")
    private Model currentModel;

    /**
     * Constructor.
     */
    public RESTAdapter(ScheduledExecutorService executor, Runnable shutdownHandler, ServiceListener serviceListener) {
        this(executor, shutdownHandler, shutdownHandler::run, serviceListener);
    }

    /**
     * Constructor.
     */
    public RESTAdapter(
            ScheduledExecutorService executor,
            Runnable shutdownHandler,
            SessionLossHandler lossHandler,
            ServiceListener serviceListener) {
        shutdownTask = () -> {
            try {
                close();
            }
            // CHECKSTYLE.OFF: IllegalCatch // Bulkhead
            catch (Exception e) {
                LOG.warn("Exception during shutdown", e);
            }
            // CHECKSTYLE.ON: IllegalCatch
            shutdownHandler.run();
        };
        containerFactory = new ContainerFactory(executor, lossHandler, serviceListener);
    }

    /**
     * Reconfigure the component.
     */
    @GuardedBy("this")
    public synchronized void reconfigure(Model model) {
        LOG.warn("Model {}", model);

        if (!model.isActive()) {
            LOG.warn("The model has been marked as inactive, shutting down");
            shutdownTask.run();
            return;
        }

        if (isFirstConfiguration()) {
            components = Components.create(containerFactory, model);
        }
        else if (isModelInactive(model)) {
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

        currentModel = model;
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

    @Override
    @GuardedBy("this")
    public synchronized void close() {
        LOG.debug("Closing adapter");
        components.close();
        currentModel = null;
        LOG.info("Closed adapter");
    }
}
