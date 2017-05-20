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

package com.pushtechnology.adapters.rest.metrics.events;

import static java.lang.System.currentTimeMillis;

import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationListener;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Listener for events about publication.
 *
 * @author Matt Champion 17/05/2017
 */
/*package*/ final class PublicationEventDispatcher implements PublicationListener {
    private final PublicationEventListener publicationEventListener;

    /**
     * Constructor.
     */
    /*package*/ PublicationEventDispatcher(PublicationEventListener publicationEventListener) {
        this.publicationEventListener = publicationEventListener;
    }

    @Override
    public PublicationCompletionListener onPublicationRequest(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            Bytes value) {
        final PublicationRequestEvent publicationRequestEvent = new PublicationRequestEvent(
            serviceConfig.getTopicPathRoot() + "/" + endpointConfig.getTopicPath(),
            value.length(),
            currentTimeMillis());

        publicationEventListener.onPublicationRequest(publicationRequestEvent);

        return new CompletionListener(publicationRequestEvent, publicationEventListener);
    }

    /**
     * Implementation of {@link PublicationCompletionListener} that notifies a
     * {@link PublicationEventListener} of events.
     */
    private static final class CompletionListener implements PublicationCompletionListener {
        private final PublicationRequestEvent publicationRequestEvent;
        private final PublicationEventListener publicationEventListener;

        private CompletionListener(
                PublicationRequestEvent publicationRequestEvent,
                PublicationEventListener publicationEventListener) {
            this.publicationRequestEvent = publicationRequestEvent;
            this.publicationEventListener = publicationEventListener;
        }

        @Override
        public void onPublication(Bytes value) {
            publicationEventListener
                .onPublicationSuccess(new PublicationSuccessEvent(publicationRequestEvent, currentTimeMillis()));
        }

        @Override
        public void onPublicationFailed(Bytes value, ErrorReason reason) {
            publicationEventListener
                .onPublicationFailed(new PublicationFailedEvent(publicationRequestEvent, reason, currentTimeMillis()));
        }
    }
}