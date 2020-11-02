/*******************************************************************************
 * Copyright (C) 2020 Push Technology Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.pushtechnology.adapters.rest.model.conversion.ConversionContext;
import com.pushtechnology.adapters.rest.model.latest.Model;

/**
 * Unit tests for {@link FileSystemPersistence}.
 *
 * @author Matt Champion 02/11/2020
 */
public final class FileSystemPersistenceTest {
    @Test
    public void loadMissingModel() throws IOException {
        final Path directory = Files.createTempDirectory("temp-persistence");
        final FileSystemPersistence persistence = new FileSystemPersistence(directory, ConversionContext.FULL_CONTEXT);

        assertFalse(persistence.loadModel().isPresent());
    }

    @Test
    public void loadModel() throws IOException {
        final Path directory = Files.createTempDirectory("temp-persistence");
        final FileSystemPersistence persistence = new FileSystemPersistence(directory, ConversionContext.FULL_CONTEXT);

        final Model model = persistence.loadModel(
            FileSystemPersistenceTest.class.getResourceAsStream("/test.json"),
            Model.class);

        assertEquals(model.getDiffusion().getHost(), "localhost");
    }
}
