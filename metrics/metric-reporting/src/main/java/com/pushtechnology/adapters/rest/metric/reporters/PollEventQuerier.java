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

import static java.util.function.Function.identity;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.event.listeners.BoundedPollEventCollector;

import net.jcip.annotations.ThreadSafe;

/**
 * A querier for poll events.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class PollEventQuerier extends CommonEventQuerier<PollRequestEvent, PollSuccessEvent, PollFailedEvent> {

    /**
     * Constructor.
     */
    public PollEventQuerier(BoundedPollEventCollector pollEventCollector) {
        super(pollEventCollector);
    }

    /**
     * @return the count of status codes received
     */
    public Map<Integer, Integer> getStatusCodes() {
        return getEventCollector()
            .getSuccessEvents()
            .stream()
            .map(PollSuccessEvent::getStatusCode)
            .collect(Collectors.toMap(identity(), code -> 1, (l, r) -> l + r, HashMap::new));
    }
}
