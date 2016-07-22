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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.publication.PublishingClient;

/**
 * The component responsible for polling REST services.
 *
 * @author Push Technology Limited
 */
/*package*/ final class PollingComponentImpl implements PollingComponent {
    private static final Logger LOG = LoggerFactory.getLogger(PollingComponentImpl.class);
    private final PublishingClient publishingClient;
    private final List<ServiceConfig> services;
    private final List<ServiceSession> serviceSessions;

    /**
     * Constructor.
     */
    /*package*/ PollingComponentImpl(
            PublishingClient publishingClient,
            List<ServiceConfig> services,
            List<ServiceSession> serviceSessions) {
        this.publishingClient = publishingClient;
        this.services = services;
        this.serviceSessions = serviceSessions;
    }

    @Override
    public void close() {
        LOG.info("Closing polling component");
        serviceSessions.forEach(ServiceSession::stop);
        services.forEach(publishingClient::removeService);
        LOG.info("Closed polling component");
    }
}
