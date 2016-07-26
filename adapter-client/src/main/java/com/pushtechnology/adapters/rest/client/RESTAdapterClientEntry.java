/*******************************************************************************
 * Copyright (C) 2016 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.client;

import static com.pushtechnology.adapters.rest.model.conversion.ConversionContext.FULL_CONTEXT;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledExecutorService;

import com.pushtechnology.adapters.rest.model.store.PollingPersistedModelStore;
import com.pushtechnology.adapters.rest.persistence.FileSystemPersistence;
import com.pushtechnology.adapters.rest.persistence.Persistence;

import net.jcip.annotations.Immutable;

/**
 * Entry point for adapter client from the command line.
 *
 * @author Push Technology Limited
 */
@Immutable
public final class RESTAdapterClientEntry {
    private RESTAdapterClientEntry() {
    }

    /**
     * Entry point for adapter client.
     * @param args The command line arguments
     * @throws IOException if there was a problem reading the persisted configuration
     * @throws InterruptedException if the thread was interrupted
     */
    // CHECKSTYLE.OFF: UncommentedMain
    public static void main(String[] args) throws IOException, InterruptedException {
        // CHECKSTYLE.ON: UncommentedMain

        final Persistence fileSystemPersistence = new FileSystemPersistence(Paths.get("."), FULL_CONTEXT);
        final ScheduledExecutorService executor = newSingleThreadScheduledExecutor();
        final PollingPersistedModelStore modelStore = new PollingPersistedModelStore(
            fileSystemPersistence,
            executor,
            1000L);

        modelStore.start();

        final RESTAdapterClient adapterClient = RESTAdapterClient.create(modelStore, executor, () -> {
            modelStore.stop();
            executor.shutdown();
        });

        adapterClient.start();
    }
}
