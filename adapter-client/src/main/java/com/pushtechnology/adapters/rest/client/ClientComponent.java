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
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.HttpComponentFactory;
import com.pushtechnology.adapters.rest.publication.PublishingClientImpl;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClientImpl;

import net.jcip.annotations.ThreadSafe;

/**
 * The component that wires together the different components.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class ClientComponent implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ClientComponent.class);
    private final PollingComponentFactory pollingComponentFactory;
    private final MutablePicoContainer topLevel = new DefaultPicoContainer();

    private MutablePicoContainer httpContainer;
    private MutablePicoContainer diffusionContainer;
    private MutablePicoContainer pollContainer;
    private Model currentModel;

    /**
     * Constructor.
     */
    public ClientComponent(ScheduledExecutorService executor, Runnable shutdownHandler) {
        topLevel.addComponent(new SessionListener(new Runnable() {
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
        }));
        pollingComponentFactory = new PollingComponentFactory(executor);
    }

    /**
     * Reconfigure the component.
     */
    public synchronized void reconfigure(Model model) throws IOException {

        if (isFirstConfiguration()) {
            initialConfiguration(model);
        }
        else if (isModelInactive(model)) {
            switchToInactiveComponents();
        }
        else if (wasInactive() || hasTruststoreChanged(model) || hasSecurityChanged(model)) {
            reconfigureAll(model);
        }
        else if (hasDiffusionChanged(model)) {
            reconfigurePollingAndPublishing(model);
        }
        else if (haveServicesChanged(model)) {
            reconfigurePolling(model);
        }

        currentModel = model;
    }

    private void initialConfiguration(Model model) throws IOException {
        LOG.info("Setting up components for the first time");

        if (!isModelInactive(model)) {
            httpContainer = newHttpContainer(model);
            diffusionContainer = newDiffusionContainer(model);
            pollContainer = newPollContainer(model);

            topLevel.start();
        }
    }

    private void switchToInactiveComponents() throws IOException {
        LOG.info("Replacing with inactive components");

        if (httpContainer != null) {
            topLevel.stop();
            httpContainer = null;
            diffusionContainer = null;
            pollContainer = null;
        }
    }

    private void reconfigureAll(Model model) throws IOException {
        LOG.info("Replacing all components");

        final MutablePicoContainer oldHttpContainer = httpContainer;

        httpContainer = newHttpContainer(model);
        diffusionContainer = newDiffusionContainer(model);
        pollContainer = newPollContainer(model);

        topLevel.start();

        if (oldHttpContainer != null) {
            oldHttpContainer.stop();
            topLevel.removeChildContainer(oldHttpContainer);
        }
    }

    private void reconfigurePollingAndPublishing(Model model) throws IOException {
        LOG.info("Replacing the polling and publishing components");

        final MutablePicoContainer oldDiffusionContainer = diffusionContainer;

        diffusionContainer = newDiffusionContainer(model);
        pollContainer = newPollContainer(model);

        diffusionContainer.start();

        if (oldDiffusionContainer != null) {
            oldDiffusionContainer.stop();
            httpContainer.removeChildContainer(oldDiffusionContainer);
        }
    }

    private void reconfigurePolling(Model model) {
        LOG.info("Replacing the polling component");

        final MutablePicoContainer oldPollContainer = pollContainer;

        pollContainer = newPollContainer(model);

        if (oldPollContainer != null) {
            oldPollContainer.stop();
            diffusionContainer.removeChildContainer(oldPollContainer);
        }
    }

    private MutablePicoContainer newHttpContainer(Model model) {
        return topLevel.makeChildContainer()
            .addAdapter(new SSLContextFactory())
            .addAdapter(new HttpComponentFactory())
            .addComponent(model);
    }

    private MutablePicoContainer newDiffusionContainer(Model model) {
        return httpContainer
            .makeChildContainer()
            .addAdapter(new SessionFactory())
            .addAdapter(new PublicationComponentFactory())
            .addComponent(PublishingClientImpl.class)
            .addComponent(TopicManagementClientImpl.class)
            .addComponent(model);
    }

    private MutablePicoContainer newPollContainer(Model model) {
        final MutablePicoContainer newContainer = diffusionContainer
            .makeChildContainer()
            .addAdapter(pollingComponentFactory)
            .addComponent(model);

        newContainer.getComponent(PollingComponent.class);

        return newContainer;
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

    private boolean wasInactive() {
        return httpContainer == null ||
            diffusionContainer == null ||
            pollContainer == null;
    }

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
    public synchronized void close() throws IOException {
        LOG.info("Closing client");
        topLevel.stop();
        LOG.info("Closed client");
    }
}
