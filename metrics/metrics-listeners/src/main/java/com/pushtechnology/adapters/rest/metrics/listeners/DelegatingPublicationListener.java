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

package com.pushtechnology.adapters.rest.metrics.listeners;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Delegating implementation of {@link PublicationListener}.
 *
 * @author Matt Champion 04/06/2017
 */
public final class DelegatingPublicationListener implements PublicationListener {
    private final Collection<PublicationListener> delegates;

    /**
     * Constructor.
     */
    public DelegatingPublicationListener(PublicationListener... delegates) {
        this.delegates = asList(delegates);
    }

    /**
     * Constructor.
     */
    public DelegatingPublicationListener(Collection<PublicationListener> delegates) {
        this.delegates = new ArrayList<>();
        this.delegates.addAll(delegates);
    }

    @Override
    public PublicationCompletionListener onPublicationRequest(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            Bytes value) {
        final List<PublicationCompletionListener> completionListeners = new ArrayList<>();
        delegates.forEach(delegate -> {
            completionListeners.add(delegate.onPublicationRequest(serviceConfig, endpointConfig, value));
        });
        return new DelegatingCompletionListener(completionListeners);
    }

    private static final class DelegatingCompletionListener implements PublicationCompletionListener {
        private final Collection<PublicationCompletionListener> delegates;

        private DelegatingCompletionListener(Collection<PublicationCompletionListener> delegates) {
            this.delegates = delegates;
        }

        @Override
        public void onPublication() {
            delegates.forEach(PublicationCompletionListener::onPublication);
        }

        @Override
        public void onPublicationFailed(ErrorReason reason) {
            delegates.forEach(delegate -> delegate.onPublicationFailed(reason));
        }
    }
}
