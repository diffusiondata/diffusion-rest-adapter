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

package com.pushtechnology.adapters.rest.adapter;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointClientImpl;
import com.pushtechnology.adapters.rest.polling.HttpClientFactoryImpl;
import com.pushtechnology.adapters.rest.polling.ServiceSessionFactoryImpl;
import com.pushtechnology.adapters.rest.publication.PublishingClientImpl;
import com.pushtechnology.adapters.rest.session.management.DiffusionSessionFactory;
import com.pushtechnology.adapters.rest.session.management.EventedSessionListener;
import com.pushtechnology.adapters.rest.session.management.SSLContextFactory;
import com.pushtechnology.adapters.rest.session.management.SessionLostListener;
import com.pushtechnology.adapters.rest.session.management.SessionWrapper;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClientImpl;

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

    private final ScheduledExecutorService executor;
    private final ServiceListener serviceListener;
    private final Runnable shutdownTask;
    private final MutablePicoContainer topLevelContainer;
    private final ServiceManager serviceManager;

    @GuardedBy("this")
    private MutablePicoContainer httpContainer;
    @GuardedBy("this")
    private MutablePicoContainer diffusionContainer;
    @GuardedBy("this")
    private MutablePicoContainer servicesContainer;
    @GuardedBy("this")
    private Model currentModel;

    /**
     * Constructor.
     */
    public RESTAdapter(ScheduledExecutorService executor, Runnable shutdownHandler, ServiceListener serviceListener) {
        this.executor = executor;
        this.serviceListener = serviceListener;
        shutdownTask = new Runnable() {
            @Override
            public void run() {
                try {
                    close();
                }
                // CHECKSTYLE.OFF: IllegalCatch // Bulkhead
                catch (Exception e) {
                    LOG.warn("Exception during shutdown", e);
                }
                // CHECKSTYLE.ON: IllegalCatch
                shutdownHandler.run();
            }
        };
        topLevelContainer = newTopLevelContainer();
        serviceManager = topLevelContainer.getComponent(ServiceManager.class);
    }

    /**
     * Reconfigure the component.
     */
    @GuardedBy("this")
    public synchronized void reconfigure(Model model) {

        if (!model.isActive()) {
            LOG.warn("The model has been marked as inactive, shutting down");
            shutdownTask.run();
            return;
        }

        if (isFirstConfiguration()) {
            initialConfiguration(model);
        }
        else if (isModelInactive(model)) {
            switchToInactiveComponents();
        }
        else if (wasInactive()) {
            reconfigureAll(model);
        }
        else if (hasTruststoreChanged(model) || hasSecurityChanged(model)) {
            reconfigureSecurity(model);
        }
        else if (hasDiffusionChanged(model)) {
            reconfigurePollingAndPublishing(model);
        }
        else if (haveServicesChanged(model)) {
            reconfigureServices(model);
        }

        currentModel = model;
    }

    @GuardedBy("this")
    private void initialConfiguration(Model model) {
        LOG.info("Setting up components");

        if (!isModelInactive(model)) {
            httpContainer = newHttpContainer(model);
            diffusionContainer = newDiffusionContainer(model);
            servicesContainer = newServicesContainer(model);

            final ServiceManagerContext managerContext = servicesContainer.getComponent(ServiceManagerContext.class);

            httpContainer.start();

            serviceManager.reconfigure(managerContext, model);
        }
    }

    @GuardedBy("this")
    private void switchToInactiveComponents() {
        LOG.info("Putting adapter to sleep");

        if (httpContainer != null) {
            httpContainer.dispose();
            httpContainer = null;
            diffusionContainer = null;
            servicesContainer = null;
        }
    }

    @GuardedBy("this")
    private void reconfigureAll(Model model) {
        LOG.info("Replacing all components");

        if (httpContainer != null) {
            httpContainer.dispose();
        }

        httpContainer = newHttpContainer(model);
        diffusionContainer = newDiffusionContainer(model);
        servicesContainer = newServicesContainer(model);

        final ServiceManagerContext managerContext = servicesContainer.getComponent(ServiceManagerContext.class);

        httpContainer.start();

        serviceManager.reconfigure(managerContext, model);
    }

    @GuardedBy("this")
    private void reconfigureSecurity(Model model) {
        LOG.info("Updating security, REST and Diffusion sessions");

        if (httpContainer != null) {
            httpContainer.dispose();
            topLevelContainer.removeChildContainer(httpContainer);
        }

        httpContainer = newHttpContainer(model);
        diffusionContainer = newDiffusionContainer(model);
        servicesContainer = newServicesContainer(model);

        final ServiceManagerContext managerContext = servicesContainer.getComponent(ServiceManagerContext.class);

        httpContainer.start();

        serviceManager.reconfigure(managerContext, model);
    }

    @GuardedBy("this")
    private void reconfigurePollingAndPublishing(Model model) {
        LOG.debug("Replacing REST and Diffusion sessions");

        if (diffusionContainer != null) {
            diffusionContainer.dispose();
            httpContainer.removeChildContainer(diffusionContainer);
        }

        diffusionContainer = newDiffusionContainer(model);
        servicesContainer = newServicesContainer(model);

        final ServiceManagerContext managerContext = servicesContainer.getComponent(ServiceManagerContext.class);

        diffusionContainer.start();

        serviceManager.reconfigure(managerContext, model);
    }

    @GuardedBy("this")
    private void reconfigureServices(Model model) {
        LOG.info("Replacing REST sessions");

        if (servicesContainer != null) {
            servicesContainer.dispose();
            diffusionContainer.removeChildContainer(servicesContainer);
        }

        servicesContainer = newServicesContainer(model);

        final ServiceManagerContext managerContext = servicesContainer.getComponent(ServiceManagerContext.class);

        servicesContainer.start();

        serviceManager.reconfigure(managerContext, model);
    }

    @GuardedBy("this")
    private MutablePicoContainer newTopLevelContainer() {
        return new PicoBuilder()
            .withCaching()
            .withConstructorInjection()
            // .withConsoleMonitor() // enable debug
            .withJavaEE5Lifecycle()
            .withLocking()
            .build()
            .addComponent(executor)
            .addComponent(serviceListener)
            .addComponent(HttpClientFactoryImpl.class)
            .addComponent(ParsingHandlerFactory.class)
            .addComponent(ServiceManager.class);
    }

    @GuardedBy("this")
    private MutablePicoContainer newHttpContainer(Model model) {
        final MutablePicoContainer newContainer = new PicoBuilder(topLevelContainer)
            .withCaching()
            .withConstructorInjection()
            // .withConsoleMonitor() // enable debug
            .withJavaEE5Lifecycle()
            .withLocking()
            .build()
            .addAdapter(new SSLContextFactory())
            .addComponent(model)
            .addComponent(EndpointClientImpl.class);
        topLevelContainer.addChildContainer(newContainer);

        return newContainer;
    }

    @GuardedBy("this")
    private MutablePicoContainer newDiffusionContainer(Model model) {
        final MutablePicoContainer newContainer = new PicoBuilder(httpContainer)
            .withCaching()
            .withConstructorInjection()
             // .withConsoleMonitor() // enable debug
            .withJavaEE5Lifecycle()
            .withLocking()
            .build()
            .addAdapter(DiffusionSessionFactory.create())
            .addComponent(TopicManagementClientImpl.class)
            .addComponent(model)
            .addComponent(shutdownTask)
            .addComponent(SessionLostListener.class)
            .addComponent(EventedSessionListener.class)
            .addComponent(SessionWrapper.class);
        httpContainer.addChildContainer(newContainer);

        return newContainer;
    }

    @GuardedBy("this")
    private MutablePicoContainer newServicesContainer(Model model) {
        final MutablePicoContainer newContainer = new PicoBuilder(diffusionContainer)
            .withCaching()
            .withConstructorInjection()
            // .withConsoleMonitor() // enable debug
            .withJavaEE5Lifecycle()
            .withLocking()
            .build()
            .addComponent(model)
            .addComponent(PublishingClientImpl.class)
            .addComponent(ServiceSessionStarterImpl.class)
            .addComponent(ServiceSessionFactoryImpl.class)
            .addComponent(ServiceManagerContext.class)
            .addComponent(EndpointPollHandlerFactoryImpl.class);
        diffusionContainer.addChildContainer(newContainer);

        return newContainer;
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
            services.stream().map(ServiceConfig::getEndpoints).flatMap(Collection::stream).collect(counting()) == 0L;
    }

    @GuardedBy("this")
    private boolean wasInactive() {
        return httpContainer == null ||
            diffusionContainer == null ||
            servicesContainer == null;
    }

    @GuardedBy("this")
    private boolean hasTruststoreChanged(Model newModel) {
        return currentModel.getTruststore() == null && newModel.getTruststore() != null ||
            !currentModel.getTruststore().equals(newModel.getTruststore());
    }

    private boolean hasSecurityChanged(Model newModel) {
        return !currentModel
            .getServices()
            .stream()
            .map(ServiceConfig::getSecurity)
            .filter(securityConfig -> securityConfig != null)
            .filter(securityConfig -> securityConfig.getBasic() != null)
            .collect(toList())
            .equals(newModel
                .getServices()
                .stream()
                .map(ServiceConfig::getSecurity)
                .filter(securityConfig -> securityConfig != null)
                .filter(securityConfig -> securityConfig.getBasic() != null)
                .collect(toList()));
    }

    @GuardedBy("this")
    private boolean hasDiffusionChanged(Model model) {
        final DiffusionConfig diffusionConfig = model.getDiffusion();

        return currentModel.getDiffusion() == null || !currentModel.getDiffusion().equals(diffusionConfig);
    }

    private boolean haveServicesChanged(Model model) {
        final List<ServiceConfig> newServices = model.getServices();
        final List<ServiceConfig> oldServices = model.getServices();

        return oldServices == null || oldServices.equals(newServices);
    }

    @Override
    @GuardedBy("this")
    public synchronized void close() {
        LOG.debug("Closing adapter");
        if (!wasInactive()) {
            httpContainer.dispose();
        }
        serviceManager.close();
        currentModel = null;
        LOG.info("Closed adapter");
    }
}
