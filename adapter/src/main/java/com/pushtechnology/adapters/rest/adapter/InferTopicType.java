/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.pushtechnology.adapters.rest.endpoints.EndpointType;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;

/**
 * Validate the content type of a response for an endpoint.
 *
 * @author Push Technology Limited
 */
public final class InferTopicType implements BiConsumer<EndpointResponse, Throwable> {
    private final Function<EndpointType<?>, BiConsumer<EndpointResponse, Throwable>> factory;

    /**
     * Constructor.
     */
    public InferTopicType(Function<EndpointType<?>, BiConsumer<EndpointResponse, Throwable>> factory) {
        this.factory = factory;
    }

    @Override
    public void accept(EndpointResponse result, Throwable throwable) {
        if (throwable != null) {
            factory.apply(EndpointType.BINARY_ENDPOINT_TYPE).accept(null, throwable);
        }
        else {
            final String contentType = result.getHeader("content-type");
            final EndpointType<?> type = EndpointType.inferFromContentType(contentType);

            factory.apply(type).accept(result, null);
        }
    }
}
