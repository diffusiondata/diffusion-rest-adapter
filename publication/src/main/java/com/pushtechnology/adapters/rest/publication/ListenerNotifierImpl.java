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

package com.pushtechnology.adapters.rest.publication;

import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener.PublicationCompletionListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Implementation of {@link ListenerNotifier}.
 *
 * @author Push Technology Limited
 */
/*package*/ final class ListenerNotifierImpl implements ListenerNotifier {
    private final PublicationListener publicationListener;
    private final ServiceConfig serviceConfig;
    private final EndpointConfig endpointConfig;

    ListenerNotifierImpl(
            PublicationListener publicationListener,
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig) {
        this.publicationListener = publicationListener;
        this.serviceConfig = serviceConfig;
        this.endpointConfig = endpointConfig;
    }

    @Override
    public PublicationCompletionListener notifyPublicationRequest(Bytes bytes) {
        return publicationListener.onPublicationRequest(serviceConfig, endpointConfig, bytes);
    }
}
