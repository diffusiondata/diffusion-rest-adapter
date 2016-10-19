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

package com.pushtechnology.adapters.rest.cloud.foundry;

import java.io.IOException;

import javax.naming.NamingException;

import com.pushtechnology.adapters.rest.integrated.server.RESTAdapterIntegratedServer;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;

/**
 * Entry point for Diffusion Cloud Foundry REST Adapter.
 *
 * @author Push Technology Limited
 */
public final class CloudFoundryRESTAdapter {
    /**
     * Entry point for Cloud Foundry REST Adapter.
     * @param args The command line arguments
     * @throws NamingException if there was a problem starting the integrated server
     * @throws IllegalStateException if there was a problem starting the integrated server
     */
    // CHECKSTYLE.OFF: UncommentedMain // Entry point for runnable JAR
    public static void main(String[] args) throws NamingException, IOException {
        // CHECKSTYLE.ON: UncommentedMain

        final VCAPServicesParser parser = new VCAPServicesParser();
        final ReapptCredentials reapptCredentials = parser
            .parse(System.getenv("VCAP_SERVICES"))
            .getReappt()
            .getCredentials();

        final String port = System.getenv("PORT");
        RESTAdapterIntegratedServer
            .create(
                port != null ? Integer.parseInt(port) : 3000,
                DiffusionConfig.builder()
                    .host(reapptCredentials.getHost())
                    .port(80)
                    .secure(false)
                    .principal(reapptCredentials.getPrincipal())
                    .password(reapptCredentials.getCredentials()))
            .start();
    }
}
