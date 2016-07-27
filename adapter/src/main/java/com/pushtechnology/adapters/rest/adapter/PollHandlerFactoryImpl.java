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

import org.apache.http.concurrent.FutureCallback;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.PollHandlerFactory;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Implementation of {@link PollHandlerFactory}.
 *
 * @author Push Technology Limited
 */
public final class PollHandlerFactoryImpl implements PollHandlerFactory {
    private final PublishingClient publishingClient;

    /**
     * Constructor.
     */
    public PollHandlerFactoryImpl(PublishingClient publishingClient) {
        this.publishingClient = publishingClient;
    }

    @Override
    public FutureCallback<JSON> create(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        return new PollPublishingHandler(
            publishingClient, serviceConfig, endpointConfig);
    }
}
