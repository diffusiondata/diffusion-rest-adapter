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

import java.util.List;

/**
 * An event collector for events.
 * <p>
 * Collects three different types of events, action
 * requests, action successes and action failures.
 *
 * @param <R> the type of request events
 * @param <S> the type of success events
 * @param <F> the type of failure events
 * @author Matt Champion 26/05/2017
 */
public interface EventCollector<R, S, F> {
    /**
     * @return the collected request events
     */
    List<R> getRequestEvents();

    /**
     * @return the collected success events
     */
    List<S> getSuccessEvents();

    /**
     * @return the collected failure events
     */
    List<F> getFailedEvents();
}
