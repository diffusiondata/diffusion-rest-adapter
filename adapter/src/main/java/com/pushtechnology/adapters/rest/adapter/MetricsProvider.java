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

package com.pushtechnology.adapters.rest.adapter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * The metrics manager.
 *
 * @author Push Technology Limited
 */
public final class MetricsProvider implements AutoCloseable {

    private final Runnable startTask;
    private final Runnable stopTask;

    /**
     * Constructor.
     */
    public MetricsProvider(
            Runnable startTask,
            Runnable stopTask) {
        this.startTask = startTask;
        this.stopTask = stopTask;
    }

    /**
     * Start logging the metrics.
     */
    @PostConstruct
    public void start() {
        startTask.run();
    }

    @PreDestroy
    @Override
    public void close() {
        stopTask.run();
    }
}
