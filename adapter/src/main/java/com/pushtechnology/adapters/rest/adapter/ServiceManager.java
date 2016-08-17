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

import java.util.ArrayList;
import java.util.List;

import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.polling.ServiceSessionFactory;

import net.jcip.annotations.ThreadSafe;

/**
 * Service manager.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class ServiceManager implements AutoCloseable {
    private Model currentModel;
    private ServiceManagerContext currentContext;
    private final List<ServiceSession> serviceSessions;

    /**
     * Constructor.
     */
    public ServiceManager() {
        serviceSessions = new ArrayList<>();
    }

    /**
     * Reconfigure the services.
     */
    public synchronized void reconfigure(ServiceManagerContext context, Model model) {
        close();

        // Apply the new configuration
        currentModel = model;
        currentContext = context;
        final ServiceSessionFactory serviceSessionFactory = currentContext.getServiceSessionFactory();
        final ServiceSessionStarter serviceSessionStarter = currentContext.getServiceSessionStarter();

        for (final ServiceConfig service : model.getServices()) {
            final ServiceSession serviceSession = serviceSessionFactory.create(service);
            serviceSessionStarter.start(service, serviceSession);
            serviceSessions.add(serviceSession);
        }
    }

    @Override
    public synchronized void close() {
        if (currentModel != null) {
            // Remove the previous configuration
            serviceSessions.forEach(ServiceSession::stop);
            currentModel.getServices().forEach(currentContext.getPublishingClient()::removeService);
            serviceSessions.clear();
            currentModel = null;
            currentContext = null;
        }
    }
}
