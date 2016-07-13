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

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;
import static java.lang.Thread.sleep;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.PublishingClient;
import com.pushtechnology.adapters.PublishingClientImpl;
import com.pushtechnology.adapters.rest.model.conversion.ConversionContext;
import com.pushtechnology.adapters.rest.model.conversion.LatestConverter;
import com.pushtechnology.adapters.rest.model.conversion.V0Converter;
import com.pushtechnology.adapters.rest.model.conversion.V1Converter;
import com.pushtechnology.adapters.rest.model.conversion.V2Converter;
import com.pushtechnology.adapters.rest.model.conversion.V3Converter;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.persistence.FileSystemPersistence;
import com.pushtechnology.adapters.rest.persistence.Persistence;
import com.pushtechnology.adapters.rest.polling.HttpClientFactoryImpl;
import com.pushtechnology.adapters.rest.polling.PollClient;
import com.pushtechnology.adapters.rest.polling.PollClientImpl;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.session.SessionFactory;

/**
 * Simple client adapting REST to Diffusion.
 *
 * @author Push Technology Limited
 */
public final class RESTAdapterClient {
    private static final Logger LOG = LoggerFactory.getLogger(RESTAdapterClient.class);

    private RESTAdapterClient() {
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
                Model.VERSION,
                Model.class,
                LatestConverter.INSTANCE)
            .build();

        final Persistence fileSystemPersistence = new FileSystemPersistence(Paths.get("."), conversionContext);

        final Optional<Model> config = fileSystemPersistence.loadModel();

        final Model model = config.orElse(
            Model
                .builder()
                .services(
                    singletonList(
                        ServiceConfig
                            .builder()
                            .host("petition.parliament.uk")
                            .port(80)
                            .endpoints(
                                singletonList(
                                    EndpointConfig
                                        .builder()
                                        .name("endpoint-0")
                                        .url("/petitions/131215.json")
                                        .topic("petitions/131215")
                                        .build()))
                            .build()))
                .build());

        LOG.debug("Configuration: {}", model);
        fileSystemPersistence.storeModel(model);

        final PublishingClient diffusionClient = new PublishingClientImpl(getSessionFactory(model.getDiffusion()));

        diffusionClient.start();

        model
            .getServices()
            .forEach(diffusionClient::initialise);

        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        final PollClient pollClient = new PollClientImpl(new HttpClientFactoryImpl());
        pollClient.start();

        model
            .getServices()
            .stream()
            .map(service -> new ServiceSession(executor, pollClient, service, diffusionClient))
            .forEach(ServiceSession::start);
    }

    private static SessionFactory getSessionFactory(DiffusionConfig diffusionConfig) {
        final SessionFactory sessionFactory = Diffusion
            .sessions()
            .serverHost(diffusionConfig.getHost())
            .serverPort(diffusionConfig.getPort())
            .secureTransport(false)
            .transports(WEBSOCKET);

        if (diffusionConfig.getPrincipal() != null && diffusionConfig.getPassword() != null) {
            return sessionFactory
                .principal(diffusionConfig.getPrincipal())
                .password(diffusionConfig.getPassword());
        }
        else  {
            return sessionFactory;
        }
    }
}
