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

/**
 * A counter for events.
 * <p>
 * Counts three different types of events, action
 * requests, action successes and action failures.
 *
 * @author Matt Champion 28/05/2017
 */
public interface EventCounter {
    /**
     * @return the number request events
     */
    int getRequests();

    /**
     * @return the number request events
     */
    int getSuccesses();

    /**
     * @return the number request events
     */
    int getFailures();
}
