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

package com.pushtechnology.adapters.rest.cloud.foundry.rest.adapter;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

import com.pushtechnology.adapters.rest.adapter.ServiceListener;
import com.pushtechnology.adapters.rest.client.RESTAdapterClient;
import com.pushtechnology.adapters.rest.client.controlled.model.store.ClientControlledModelStore;
import com.pushtechnology.adapters.rest.cloud.foundry.vcap.ReapptCredentials;
import com.pushtechnology.adapters.rest.cloud.foundry.vcap.VCAP;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.session.management.SSLContextFactory;
import com.pushtechnology.diffusion.client.session.SessionAttributes;

/**
 * Entry point for Diffusion Cloud Foundry REST Adapter.
 *
 * @author Push Technology Limited
 */
public final class CloudFoundryRESTAdapter {
    private CloudFoundryRESTAdapter() {
    }

    /**
     * Entry point for Cloud Foundry REST Adapter.
     * @param args The command line arguments
     * @throws NamingException if there was a problem starting the integrated server
     * @throws IllegalStateException if there was a problem starting the integrated server
     */
    // CHECKSTYLE.OFF: UncommentedMain // Entry point for runnable JAR
    public static void main(String[] args) throws NamingException, IOException {
        // CHECKSTYLE.ON: UncommentedMain

        final ReapptCredentials reapptCredentials = VCAP.getServices()
            .getReappt()
            .getCredentials();

        final SSLContext sslContext = SSLContextFactory.loadFromResource("reapptTruststore.jks");

        final ScheduledExecutorService executor = newSingleThreadScheduledExecutor();

        final DiffusionConfig diffusionConfig = DiffusionConfig
            .builder()
            .host(reapptCredentials.getHost())
            .port(443)
            .secure(true)
            .principal(reapptCredentials.getPrincipal())
            .password(reapptCredentials.getCredentials())
            .connectionTimeout(SessionAttributes.DEFAULT_CONNECTION_TIMEOUT)
            .reconnectionTimeout(SessionAttributes.DEFAULT_RECONNECTION_TIMEOUT)
            .maximumMessageSize(SessionAttributes.DEFAULT_MAXIMUM_MESSAGE_SIZE)
            .inputBufferSize(SessionAttributes.DEFAULT_INPUT_BUFFER_SIZE)
            .outputBufferSize(SessionAttributes.DEFAULT_OUTPUT_BUFFER_SIZE)
            .recoveryBufferSize(SessionAttributes.DEFAULT_RECOVERY_BUFFER_SIZE)
            .build();

        final ClientControlledModelStore modelStore = ClientControlledModelStore
            .create(executor, diffusionConfig, sslContext);

        modelStore.start();

        final RESTAdapterClient adapterClient = RESTAdapterClient.create(
            modelStore,
            executor,
            executor::shutdown,
            ServiceListener.NULL_LISTENER);

        adapterClient.start();
    }
}
