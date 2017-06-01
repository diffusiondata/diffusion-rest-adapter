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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract implementation of {@link EventCounter}.
 *
 * @author Matt Champion 28/05/2017
 */
/*package*/ abstract class AbstractEventCounter implements EventCounter {
    private final AtomicInteger requests = new AtomicInteger(0);
    private final AtomicInteger failures = new AtomicInteger(0);
    private final AtomicInteger successes = new AtomicInteger(0);

    @Override
    public int getRequests() {
        return requests.get();
    }

    @Override
    public int getSuccesses() {
        return successes.get();
    }

    @Override
    public int getFailures() {
        return failures.get();
    }

    /**
     * Increases the count of requests.
     */
    protected void onRequest() {
        requests.incrementAndGet();
    }

    /**
     * Increases the count of successes.
     */
    protected void onSuccess() {
        successes.incrementAndGet();
    }

    /**
     * Increases the count of failures.
     */
    protected void onFailure() {
        failures.incrementAndGet();
    }
}
