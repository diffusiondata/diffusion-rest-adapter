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

package com.pushtechnology.adapters.rest.metric.reporters;

import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPublicationEventCollector;

import net.jcip.annotations.ThreadSafe;

/**
 * A querier for publication events.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class PublicationEventQuerier
        extends CommonEventQuerier<PublicationRequestEvent, PublicationSuccessEvent, PublicationFailedEvent> {

    /**
     * Constructor.
     */
    public PublicationEventQuerier(BoundedPublicationEventCollector publicationEventCollector) {
        super(publicationEventCollector);
    }
}
