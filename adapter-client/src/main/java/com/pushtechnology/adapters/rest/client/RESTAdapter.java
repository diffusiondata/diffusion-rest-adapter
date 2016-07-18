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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import com.pushtechnology.adapters.rest.model.conversion.ConversionContext;
import com.pushtechnology.adapters.rest.model.conversion.LatestConverter;
import com.pushtechnology.adapters.rest.model.conversion.V0Converter;
import com.pushtechnology.adapters.rest.model.conversion.V1Converter;
import com.pushtechnology.adapters.rest.model.conversion.V2Converter;
import com.pushtechnology.adapters.rest.model.conversion.V3Converter;
import com.pushtechnology.adapters.rest.model.conversion.V4Converter;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.store.FixedModelStore;
import com.pushtechnology.adapters.rest.model.store.ModelStore;
import com.pushtechnology.adapters.rest.persistence.FileSystemPersistence;
import com.pushtechnology.adapters.rest.persistence.Persistence;

import net.jcip.annotations.Immutable;

/**
 * Entry point for adapter client from the command line.
 *
 * @author Push Technology Limited
 */
@Immutable
public final class RESTAdapter {
    private RESTAdapter() {
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
        final ConversionContext conversionContext = ConversionContext
            .builder()
            .register(
                com.pushtechnology.adapters.rest.model.v0.Model.VERSION,
                com.pushtechnology.adapters.rest.model.v0.Model.class,
                V0Converter.INSTANCE)
            .register(
                com.pushtechnology.adapters.rest.model.v1.Model.VERSION,
                com.pushtechnology.adapters.rest.model.v1.Model.class,
                V1Converter.INSTANCE)
            .register(
                com.pushtechnology.adapters.rest.model.v2.Model.VERSION,
                com.pushtechnology.adapters.rest.model.v2.Model.class,
                V2Converter.INSTANCE)
            .register(
                com.pushtechnology.adapters.rest.model.v3.Model.VERSION,
                com.pushtechnology.adapters.rest.model.v3.Model.class,
                V3Converter.INSTANCE)
            .register(
                com.pushtechnology.adapters.rest.model.v4.Model.VERSION,
                com.pushtechnology.adapters.rest.model.v4.Model.class,
                V4Converter.INSTANCE)
            .register(
                Model.VERSION,
                Model.class,
                LatestConverter.INSTANCE)
            .build();

        final Persistence fileSystemPersistence = new FileSystemPersistence(Paths.get("."), conversionContext);

        final Optional<Model> config = fileSystemPersistence.loadModel();

        final Model model = config.orElseThrow(() -> new IllegalStateException("No model found to use"));
        final ModelStore modelStore = new FixedModelStore(model);

        final RESTAdapterClient adapterClient = RESTAdapterClient.create(modelStore);

        adapterClient.start();
    }
}
