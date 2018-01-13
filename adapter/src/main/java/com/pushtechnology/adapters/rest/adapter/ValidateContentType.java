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

import java.util.function.BiFunction;

import com.pushtechnology.adapters.rest.endpoints.EndpointType;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;

/**
 * Validate the content type of a response for an endpoint.
 *
 * @author Push Technology Limited
 */
public final class ValidateContentType implements BiFunction<EndpointResponse, EndpointConfig, EndpointResponse> {
    /**
     * Constructor.
     */
    public ValidateContentType() {
    }

    @Override
    public EndpointResponse apply(EndpointResponse result, EndpointConfig endpointConfig) {
        final String contentType = result.getHeader("content-type");
        if (contentType == null) {
            // Assume correct when no content type provided
            return result;
        }

        final EndpointType<?> type = EndpointType.from(endpointConfig.getProduces());

        if (type.canHandle(contentType)) {
            return result;
        }

        throw new IllegalArgumentException(
            "The content type of the response " +
                contentType +
                " is not suitable for the endpoint " +
                endpointConfig);
    }
}
