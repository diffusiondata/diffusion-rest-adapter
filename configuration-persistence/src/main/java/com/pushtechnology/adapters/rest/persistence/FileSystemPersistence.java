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

package com.pushtechnology.adapters.rest.persistence;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.conversion.ConversionContext;
import com.pushtechnology.adapters.rest.model.latest.Model;

/**
 * Persist to the file system.
 *
 * @author Push Technology Limited
 */
public final class FileSystemPersistence implements Persistence {
    private final JsonFactory jsonFactory = new JsonFactory();
    private final ObjectMapper objectMapper = new ObjectMapper(jsonFactory).enable(SerializationFeature.INDENT_OUTPUT);
    private final ConversionContext conversionContext;
    private final Path configFilePath;
    private final Path versionFilePath;

    /**
     * Constructor.
     */
    public FileSystemPersistence(Path directoryPath, ConversionContext conversionContext) {
        this.conversionContext = conversionContext;
        configFilePath = directoryPath.resolve("service.json");
        versionFilePath = directoryPath.resolve("service.version.json");
    }

    @Override
    public Optional<Model> loadModel() throws IOException {
        if (!configFilePath.toFile().exists() || !versionFilePath.toFile().exists()) {
            return Optional.empty();
        }

        final int version = loadVersion();
        final Model model = conversionContext.convert(loadModel(conversionContext.modelVersion(version)));

        return Optional.of(model);
    }

    private int loadVersion() throws IOException {
        final Integer version;
        try (InputStream inputStream = newInputStream(versionFilePath, READ)) {
            try (JsonParser parser = jsonFactory.createParser(inputStream)) {
                version = objectMapper.readValue(parser, Integer.class);
            }
        }

        return version;
    }

    private <T extends AnyModel> T loadModel(Class<T> modelVersion) throws IOException {
        try (InputStream inputStream = newInputStream(configFilePath, READ)) {
            try (JsonParser parser = jsonFactory.createParser(inputStream)) {
                return objectMapper.readValue(parser, modelVersion);
            }
        }
    }

    @Override
    public void storeModel(Model model) throws IOException {
        storeServices(model);
        storeSchemaVersion();
    }

    private void storeServices(Model model) throws IOException {
        try (OutputStream configStream = newOutputStream(configFilePath, CREATE, WRITE)) {
            objectMapper.writeValue(configStream, model);
        }
    }

    private void storeSchemaVersion() throws IOException {
        try (OutputStream versionStream = newOutputStream(versionFilePath, CREATE, WRITE)) {
            objectMapper.writeValue(versionStream, Model.VERSION);
        }
    }
}
