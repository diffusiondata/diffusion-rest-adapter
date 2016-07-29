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

package com.pushtechnology.adapters.rest.polling;

import java.util.concurrent.ScheduledExecutorService;

import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Implementation of {@link ServiceSessionFactory}.
 *
 * @author Push Technology Limited
 */
public final class ServiceSessionFactoryImpl implements ServiceSessionFactory {
    private final ScheduledExecutorService executor;
    private final EndpointClient endpointClient;
    private final EndpointPollHandlerFactory handlerFactory;

    /**
     * Constructor.
     */
    public ServiceSessionFactoryImpl(
            ScheduledExecutorService executor,
            EndpointClient endpointClient,
            EndpointPollHandlerFactory handlerFactory) {
        this.executor = executor;
        this.endpointClient = endpointClient;
        this.handlerFactory = handlerFactory;
    }

    @Override
    public ServiceSession create(ServiceConfig serviceConfig) {
        return new ServiceSessionImpl(executor, endpointClient, serviceConfig, handlerFactory);
    }
}
