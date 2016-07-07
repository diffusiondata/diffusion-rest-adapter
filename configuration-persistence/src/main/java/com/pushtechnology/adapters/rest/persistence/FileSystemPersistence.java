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
import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.conversion.ConversionContext;
import com.pushtechnology.adapters.rest.model.v1.Model;

/**
 * Persist to the file system.
 *
 * @author Push Technology Limited
 */
public final class FileSystemPersistence implements Persistence {
    private final JsonFactory jsonFactory = new JsonFactory();
    private final ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
    private final ConversionContext conversionContext;
    private final Path configFilePath;
    private final Path versionFilePath;

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
            objectMapper.writeValue(versionStream, 1);
        }
    }
}
