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

import java.util.function.Function;

import com.pushtechnology.adapters.rest.endpoints.EndpointType;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;

import kotlin.Pair;

/**
 * Validate the content type of a response for an endpoint.
 *
 * @author Push Technology Limited
 */
public final class ValidateContentType
        implements Function<Pair<EndpointConfig, EndpointResponse>, Pair<EndpointConfig, EndpointResponse>> {
    /**
     * Constructor.
     */
    public ValidateContentType() {
    }

    @Override
    public Pair<EndpointConfig, EndpointResponse> apply(Pair<EndpointConfig, EndpointResponse> configAndResult) {
        final String contentType = configAndResult.getSecond().getHeader("content-type");
        if (contentType == null) {
            // Assume correct when no content type provided
            return configAndResult;
        }

        final EndpointType<?> type = EndpointType.from(configAndResult.getFirst().getProduces());

        if (type.canHandle(contentType)) {
            return configAndResult;
        }

        throw new IllegalArgumentException(
            "The content type of the response " +
                contentType +
                " is not suitable for the endpoint " +
                configAndResult.getFirst());
    }
}
