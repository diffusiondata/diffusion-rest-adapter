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

import com.pushtechnology.adapters.rest.polling.ServiceSessionFactory;
import com.pushtechnology.adapters.rest.publication.PublishingClient;

import net.jcip.annotations.Immutable;

/**
 * The context needed for creating, starting and stopping ServiceSessions.
 *
 * @author Push Technology Limited
 */
@Immutable
public final class ServiceManagerContext {
    private final PublishingClient publishingClient;
    private final ServiceSessionFactory serviceSessionFactory;
    private final ServiceSessionStarter serviceSessionStarter;

    /**
     * Constructor.
     */
    public ServiceManagerContext(
            PublishingClient publishingClient,
            ServiceSessionFactory serviceSessionFactory,
            ServiceSessionStarter serviceSessionStarter) {
        this.publishingClient = publishingClient;
        this.serviceSessionFactory = serviceSessionFactory;
        this.serviceSessionStarter = serviceSessionStarter;
    }

    /**
     * @return the publishing client
     */
    public PublishingClient getPublishingClient() {
        return publishingClient;
    }

    /**
     * @return the publishing client
     */
    public ServiceSessionFactory getServiceSessionFactory() {
        return serviceSessionFactory;
    }

    /**
     * @return the publishing client
     */
    public ServiceSessionStarter getServiceSessionStarter() {
        return serviceSessionStarter;
    }
}