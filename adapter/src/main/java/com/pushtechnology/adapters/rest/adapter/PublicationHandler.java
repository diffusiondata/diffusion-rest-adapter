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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.publication.UpdateContext;

/**
 * Handler for a poll request that publishes the response using a {@link UpdateContext}.
 *
 * @param <T> the type of values it publishes
 * @author Push Technology Limited
 */
/*package*/ final class PublicationHandler<T> implements FutureCallback<T> {
    private static final Logger LOG = LoggerFactory.getLogger(PublicationHandler.class);
    private final EndpointConfig endpointConfig;
    private final UpdateContext<T> updateContext;

    /*package*/ PublicationHandler(
            EndpointConfig endpointConfig,
            UpdateContext<T> updateContext) {

        this.endpointConfig = endpointConfig;
        this.updateContext = updateContext;
    }

    @Override
    public void completed(T result) {
        updateContext.publish(result);
    }

    @Override
    public void failed(Exception ex) {
        LOG.warn("Failed to poll endpoint {}", endpointConfig, ex);
    }

    @Override
    public void cancelled() {
        LOG.debug("Polling cancelled for endpoint {}", endpointConfig);
    }
}
