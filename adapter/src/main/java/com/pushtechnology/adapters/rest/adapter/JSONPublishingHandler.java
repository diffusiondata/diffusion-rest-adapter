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
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Handler for a poll request that publishes the response.
 *
 * @author Push Technology Limited
 */
/*package*/ final class JSONPublishingHandler implements FutureCallback<JSON> {
    private static final Logger LOG = LoggerFactory.getLogger(JSONPublishingHandler.class);
    private final PublishingClient publishingClient;
    private final ServiceConfig serviceConfig;
    private final EndpointConfig endpointConfig;

    /*package*/ JSONPublishingHandler(
            PublishingClient publishingClient,
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig) {

        this.publishingClient = publishingClient;
        this.serviceConfig = serviceConfig;
        this.endpointConfig = endpointConfig;
    }

    @Override
    public void completed(JSON result) {
        publishingClient.publish(serviceConfig, endpointConfig, result);
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
