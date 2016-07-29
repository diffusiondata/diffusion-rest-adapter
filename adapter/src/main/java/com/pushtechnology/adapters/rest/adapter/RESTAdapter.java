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

import java.io.IOException;
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

    @GuardedBy("this")
    private MutablePicoContainer topLevelContainer;
    @GuardedBy("this")
    private MutablePicoContainer httpContainer;
    @GuardedBy("this")
    private MutablePicoContainer diffusionContainer;
    @GuardedBy("this")
    private MutablePicoContainer pollContainer;
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
                catch (IOException e) {
                    // Not expected as no known implementation throws this
                    LOG.warn("Exception during shutdown", e);
                }
                shutdownHandler.run();
            }

        };
    }

    /**
     * Reconfigure the component.
     */
    @GuardedBy("this")
    public synchronized void reconfigure(Model model) throws IOException {

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
            reconfigurePolling(model);
        }

        currentModel = model;
    }

    @GuardedBy("this")
    private void initialConfiguration(Model model) throws IOException {
        LOG.info("Setting up components for the first time");

        if (!isModelInactive(model)) {
            topLevelContainer = newTopLevelContainer();
            httpContainer = newHttpContainer(model);
            diffusionContainer = newDiffusionContainer(model);
            pollContainer = newPollContainer(model);

            LOG.info("Opening components");
            topLevelContainer.start();
        }
    }

    @GuardedBy("this")
    private void switchToInactiveComponents() throws IOException {
        LOG.info("Replacing with inactive components");

        if (topLevelContainer != null) {
            topLevelContainer.dispose();
            topLevelContainer = null;
            httpContainer = null;
            diffusionContainer = null;
            pollContainer = null;
        }
    }

    @GuardedBy("this")
    private void reconfigureAll(Model model) throws IOException {
        LOG.info("Replacing all components");

        final MutablePicoContainer oldTopLevelContainer = topLevelContainer;

        topLevelContainer = newTopLevelContainer();
        httpContainer = newHttpContainer(model);
        diffusionContainer = newDiffusionContainer(model);
        pollContainer = newPollContainer(model);

        topLevelContainer.start();

        if (oldTopLevelContainer != null) {
            oldTopLevelContainer.dispose();
        }
    }

    @GuardedBy("this")
    private void reconfigureSecurity(Model model) throws IOException {
        LOG.info("Updating security, replacing the polling and publishing components");

        final MutablePicoContainer oldHttpContainer = httpContainer;

        httpContainer = newHttpContainer(model);
        diffusionContainer = newDiffusionContainer(model);
        pollContainer = newPollContainer(model);

        httpContainer.start();

        if (oldHttpContainer != null) {
            oldHttpContainer.dispose();
            topLevelContainer.removeChildContainer(oldHttpContainer);
        }
    }

    @GuardedBy("this")
    private void reconfigurePollingAndPublishing(Model model) throws IOException {
        LOG.info("Replacing the polling and publishing components");

        final MutablePicoContainer oldDiffusionContainer = diffusionContainer;

        diffusionContainer = newDiffusionContainer(model);
        pollContainer = newPollContainer(model);

        diffusionContainer.start();

        if (oldDiffusionContainer != null) {
            oldDiffusionContainer.dispose();
            httpContainer.removeChildContainer(oldDiffusionContainer);
        }
    }

    @GuardedBy("this")
    private void reconfigurePolling(Model model) {
        LOG.info("Replacing the polling component");

        final MutablePicoContainer oldPollContainer = pollContainer;

        LOG.info("Starting new polling component");
        pollContainer = newPollContainer(model);
        pollContainer.start();

        if (oldPollContainer != null) {
            LOG.info("Stopping old polling component");
            oldPollContainer.dispose();
            diffusionContainer.removeChildContainer(oldPollContainer);
        }
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
            .addComponent(HttpClientFactoryImpl.class);
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
            .addComponent(PublishingClientImpl.class)
            .addComponent(TopicManagementClientImpl.class)
            .addComponent(JSONPollHandlerFactory.class)
            .addComponent(model)
            .addComponent(shutdownTask)
            .addComponent(SessionLostListener.class)
            .addComponent(SessionWrapper.class);
        httpContainer.addChildContainer(newContainer);

        return newContainer;
    }

    @GuardedBy("this")
    private MutablePicoContainer newPollContainer(Model model) {
        final MutablePicoContainer newContainer = new PicoBuilder(diffusionContainer)
            .withCaching()
            .withConstructorInjection()
            // .withConsoleMonitor() // enable debug
            .withJavaEE5Lifecycle()
            .withLocking()
            .build()
            .addComponent(model)
            .addComponent(ServiceSessionFactoryImpl.class)
            .addComponent(ServiceSessionGroupImpl.class);
        diffusionContainer.addChildContainer(newContainer);

        newContainer.getComponent(ServiceSessionGroup.class);

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
            pollContainer == null;
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
            .collect(toList())
            .equals(newModel
                .getServices()
                .stream()
                .map(ServiceConfig::getSecurity)
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
    public synchronized void close() throws IOException {
        LOG.info("Closing adapter");
        if (!wasInactive()) {
            topLevelContainer.dispose();
        }
        LOG.info("Closed adapter");
    }
}
