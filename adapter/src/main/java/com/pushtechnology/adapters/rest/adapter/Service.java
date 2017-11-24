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

import static java.util.Objects.requireNonNull;

import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.services.ServiceSession;
import com.pushtechnology.adapters.rest.services.ServiceSessionFactory;
import com.pushtechnology.adapters.rest.publication.PublishingClient;

import net.jcip.annotations.NotThreadSafe;

/**
 * State of interactions with a REST service.
 *
 * @author Push Technology Limited
 */
@NotThreadSafe
public final class Service implements AutoCloseable {
    private PublishingClient publishingClient;
    private ServiceSession serviceSession;
    private ServiceConfig serviceConfig;

    /**
     * Constructor.
     */
    /*package*/ Service() {
    }

    /**
     * Reconfigure the service.
     */
    public void reconfigure(ServiceConfig newServiceConfig, ServiceManagerContext context) {
        requireNonNull(newServiceConfig, "Service configuration must be provided");
        requireNonNull(context, "A service context must be provided");

        assert serviceConfig == null || serviceConfig.getName().equals(newServiceConfig.getName()) :
            "Services cannot be renamed. It is expected that they will be treated as a remove and add.";

        // Stop the current behaviour
        close();

        // Start the new behaviour
        publishingClient = context.getPublishingClient();
        serviceConfig = newServiceConfig;

        final ServiceSessionFactory serviceSessionFactory = context.getServiceSessionFactory();
        final ServiceSessionStarter serviceSessionStarter = context.getServiceSessionStarter();
        serviceSession = serviceSessionFactory.create(serviceConfig);
        serviceSessionStarter.start(serviceConfig, serviceSession);
    }

    @Override
    public void close() {
        if (serviceSession != null) {
            // Stop polling of REST service
            serviceSession.stop();
            serviceSession = null;
        }

        if (serviceConfig != null) {
            // Release the update source
            publishingClient.removeService(serviceConfig);
            serviceConfig = null;
        }
    }
}
