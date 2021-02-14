/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.metrics.event.listeners;

import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;

/**
 * Listener for events about publication.
 *
 * @author Push Technology Limited
 */
public final class PublicationEventDispatcher implements PublicationListener {
    private final PublicationEventListener publicationEventListener;

    /**
     * Constructor.
     */
    public PublicationEventDispatcher(PublicationEventListener publicationEventListener) {
        this.publicationEventListener = publicationEventListener;
    }

    @Override
    public PublicationCompletionListener onPublicationRequest(String path, int size) {
        final PublicationRequestEvent publicationRequestEvent = PublicationRequestEvent.Factory.create(path, size);

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
        public void onPublication() {
            publicationEventListener
                .onPublicationSuccess(PublicationSuccessEvent.Factory.create(publicationRequestEvent));
        }

        @Override
        public void onPublicationFailed(ErrorReason reason) {
            publicationEventListener
                .onPublicationFailed(PublicationFailedEvent.Factory.create(publicationRequestEvent, reason));
        }
    }
}
