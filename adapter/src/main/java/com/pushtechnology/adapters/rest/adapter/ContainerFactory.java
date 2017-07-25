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

import java.util.concurrent.ScheduledExecutorService;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import com.pushtechnology.adapters.rest.model.latest.Model;
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
 * Factory for pico containers containing the components of the REST adapter.
 *
 * @author Matt Champion 26/07/2017
 */
@ThreadSafe
/*package*/ final class ContainerFactory {
    private final ScheduledExecutorService executor;
    private final ServiceListener serviceListener;
    private final Runnable shutdownTask;

    /**
     * Constructor.
     */
    public ContainerFactory(ScheduledExecutorService executor, Runnable shutdownTask, ServiceListener serviceListener) {
        this.executor = executor;
        this.serviceListener = serviceListener;
        this.shutdownTask = shutdownTask;
    }

    @GuardedBy("this")
    MutablePicoContainer newTopLevelContainer() {
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
            .addComponent(ServiceManager.class)
            .addComponent(EventedSessionListener.class);
    }

    @GuardedBy("this")
    MutablePicoContainer newTLSContainer(Model model, MutablePicoContainer parent) {
        final MutablePicoContainer newContainer = new PicoBuilder(parent)
            .withCaching()
            .withConstructorInjection()
            // .withConsoleMonitor() // enable debug
            .withJavaEE5Lifecycle()
            .withLocking()
            .build()
            .addAdapter(new SSLContextFactory())
            .addComponent(model);
        parent.addChildContainer(newContainer);

        return newContainer;
    }

    @GuardedBy("this")
    MutablePicoContainer newHttpContainer(Model model, MutablePicoContainer parent) {
        final MutablePicoContainer newContainer = new PicoBuilder(parent)
            .withCaching()
            .withConstructorInjection()
            // .withConsoleMonitor() // enable debug
            .withJavaEE5Lifecycle()
            .withLocking()
            .build()
            .addComponent(model)
            .addComponent(EndpointClientImpl.class);
        parent.addChildContainer(newContainer);

        return newContainer;
    }

    @GuardedBy("this")
    MutablePicoContainer newDiffusionContainer(Model model, MutablePicoContainer parent) {
        final MutablePicoContainer newContainer = new PicoBuilder(parent)
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
            .addComponent(SessionWrapper.class);
        parent.addChildContainer(newContainer);

        return newContainer;
    }

    @GuardedBy("this")
    MutablePicoContainer newServicesContainer(Model model, MutablePicoContainer parent) {
        final MutablePicoContainer newContainer = new PicoBuilder(parent)
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
        parent.addChildContainer(newContainer);

        return newContainer;
    }
}
