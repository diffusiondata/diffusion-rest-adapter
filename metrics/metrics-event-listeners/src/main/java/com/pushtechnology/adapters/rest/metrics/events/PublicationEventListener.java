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

import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;

/**
 * Listener for events about publication.
 *
 * @author Matt Champion 17/05/2017
 */
public interface PublicationEventListener {
    /**
     * Notified when an attempt to publish to a topic is made.
     *
     * @param event the event
     */
    void onPublicationRequest(PublicationRequestEvent event);

    /**
     * Notified when a publication completes.
     *
     * @param event the event
     */
    void onPublicationSuccess(PublicationSuccessEvent event);

    /**
     * Notified when a publication fails.
     *
     * @param event the event
     */
    void onPublicationFailed(PublicationFailedEvent event);
}
