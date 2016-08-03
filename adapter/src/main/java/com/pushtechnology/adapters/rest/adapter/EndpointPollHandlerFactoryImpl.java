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
import com.pushtechnology.adapters.rest.polling.BinaryParsingHandler;
import com.pushtechnology.adapters.rest.polling.EndpointPollHandlerFactory;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.adapters.rest.polling.JSONParsingHandler;
import com.pushtechnology.adapters.rest.polling.StringParsingHandler;
import com.pushtechnology.adapters.rest.publication.PublishingClient;

/**
 * Implementation of {@link EndpointPollHandlerFactory}.
 *
 * @author Push Technology Limited
 */
public final class EndpointPollHandlerFactoryImpl implements EndpointPollHandlerFactory {
    private final PublishingClient publishingClient;

    /**
     * Constructor.
     */
    public EndpointPollHandlerFactoryImpl(PublishingClient publishingClient) {
        this.publishingClient = publishingClient;
    }

    @Override
    public FutureCallback<EndpointResponse> create(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        final String produces = endpointConfig.getProduces();
        switch (produces) {
            case "json" :
            case "application/json" :
                return new StringParsingHandler(
                    new JSONParsingHandler(
                        new JSONPublishingHandler(publishingClient, serviceConfig, endpointConfig)));

            case "binary" :
                return new BinaryParsingHandler(
                    new BinaryPublishingHandler(publishingClient, serviceConfig, endpointConfig));

            case "string" :
            case "text/plain" :
                return new StringParsingHandler(
                    new StringPublishingHandler(publishingClient, serviceConfig, endpointConfig));

            default:
                throw new IllegalArgumentException("Unsupported produces value \"" + produces + "\"");
        }
    }
}
