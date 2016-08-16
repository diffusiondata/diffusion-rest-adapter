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

        RESTAdapterClient.create(Paths.get(".")).start();
    }
}
