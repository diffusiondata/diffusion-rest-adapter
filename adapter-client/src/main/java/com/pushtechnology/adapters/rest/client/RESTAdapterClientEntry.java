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

package com.pushtechnology.adapters.rest.client;

import static java.lang.String.join;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.Immutable;

/**
 * Entry point for adapter client from the command line.
 *
 * @author Push Technology Limited
 */
@Immutable
public final class RESTAdapterClientEntry {
    private static final Logger LOG = LoggerFactory.getLogger(RESTAdapterClientEntry.class);

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

        final Path pathToConfigDirectory;
        if (args.length == 0) {
            pathToConfigDirectory = Paths.get(".").toAbsolutePath();
        }
        else if (args.length == 1) {
            pathToConfigDirectory = Paths.get(args[0]).toAbsolutePath();
        }
        else {
            LOG.error(
                "Expected 0 or 1 arguments for the path to the configuration directory. Was passed {}",
                join(", ", args));
            System.exit(1);
            return;
        }

        if (!Files.exists(pathToConfigDirectory)) {
            LOG.error("The path to the configuration directory {} does not exist", pathToConfigDirectory);
            System.exit(1);
            return;
        }

        if (!Files.isDirectory(pathToConfigDirectory)) {
            LOG.error("The path to the configuration directory {} is not a directory", pathToConfigDirectory);
            System.exit(1);
            return;
        }

        LOG.info("Searching for configuration in {}", pathToConfigDirectory);

        RESTAdapterClient.create(pathToConfigDirectory).start();
    }
}
