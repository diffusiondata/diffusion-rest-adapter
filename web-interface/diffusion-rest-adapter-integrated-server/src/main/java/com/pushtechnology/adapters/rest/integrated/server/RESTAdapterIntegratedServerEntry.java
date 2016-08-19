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

package com.pushtechnology.adapters.rest.integrated.server;

import javax.naming.NamingException;

/**
 * Entry point for Diffusion REST Adapter Integrated Server.
 *
 * @author Push Technology Limited
 */
public final class RESTAdapterIntegratedServerEntry {
    private RESTAdapterIntegratedServerEntry() {
    }

    /**
     * Entry point for adapter client.
     * @param args The command line arguments
     * @throws NamingException if there was a problem starting the integrated server
     * @throws IllegalStateException if there was a problem starting the integrated server
     */
    // CHECKSTYLE.OFF: UncommentedMain // Entry point for runnable JAR
    public static void main(String[] args) throws NamingException {
        // CHECKSTYLE.ON: UncommentedMain

        RESTAdapterIntegratedServer.create(8080).start();
    }
}
