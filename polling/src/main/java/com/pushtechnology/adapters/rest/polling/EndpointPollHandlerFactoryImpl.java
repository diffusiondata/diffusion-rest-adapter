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

import org.apache.http.concurrent.FutureCallback;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Implementation of {@link EndpointPollHandlerFactory}.
 *
 * @author Push Technology Limited
 */
public final class EndpointPollHandlerFactoryImpl implements EndpointPollHandlerFactory {
    private final PollHandlerFactory<JSON> jsonHandlerFactory;
    private final PollHandlerFactory<Binary> binaryHandlerFactory;
    private final PollHandlerFactory<String> stringHandlerFactory;

    /**
     * Constructor.
     */
    public EndpointPollHandlerFactoryImpl(
            JSONPollHandlerFactory jsonHandlerFactory,
            BinaryPollHandlerFactory binaryHandlerFactory,
            StringPollHandlerFactory stringHandlerFactory) {
        this.jsonHandlerFactory = jsonHandlerFactory;
        this.binaryHandlerFactory = binaryHandlerFactory;
        this.stringHandlerFactory = stringHandlerFactory;
    }

    @Override
    public FutureCallback<String> create(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        final String produces = endpointConfig.getProduces();
        switch (produces) {
            case "json" :
            case "application/json" :
                return new JSONParsingHandler(jsonHandlerFactory.create(serviceConfig, endpointConfig));

            case "binary" :
                return new BinaryParsingHandler(binaryHandlerFactory.create(serviceConfig, endpointConfig));

            case "string" :
            case "text/plain" :
                return stringHandlerFactory.create(serviceConfig, endpointConfig);

            default:
                throw new IllegalArgumentException("Unsupported produces value \"" + produces + "\"");
        }
    }
}
