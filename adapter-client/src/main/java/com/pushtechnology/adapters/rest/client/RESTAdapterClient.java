package com.pushtechnology.adapters.rest.client;

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
import com.pushtechnology.adapters.rest.model.conversion.V0Converter;
import com.pushtechnology.adapters.rest.model.conversion.V1Converter;
import com.pushtechnology.adapters.rest.model.conversion.V2Converter;
import com.pushtechnology.adapters.rest.model.conversion.V3Converter;
import com.pushtechnology.adapters.rest.model.conversion.V4Converter;
import com.pushtechnology.adapters.rest.model.v4.Endpoint;
import com.pushtechnology.adapters.rest.model.v4.Model;
import com.pushtechnology.adapters.rest.model.v4.Service;
import com.pushtechnology.adapters.rest.persistence.FileSystemPersistence;
import com.pushtechnology.adapters.rest.persistence.Persistence;
import com.pushtechnology.adapters.rest.polling.HttpClientFactoryImpl;
import com.pushtechnology.adapters.rest.polling.PollClient;
import com.pushtechnology.adapters.rest.polling.PollClientImpl;
import com.pushtechnology.adapters.rest.polling.ServiceSession;

/**
 * Simple client adapting REST to Diffusion.
 *
 * @author Push Technology Limited
 */
public class RESTAdapterClient {
    private static final Logger LOG = LoggerFactory.getLogger(RESTAdapterClient.class);

    public static void main(String[] args) throws IOException, InterruptedException {
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
                V4Converter.INSTANCE)
            .build();

        final Persistence fileSystemPersistence = new FileSystemPersistence(Paths.get("."), conversionContext);

        final Optional<Model> config = fileSystemPersistence.loadModel();

        final Model model = config.orElse(
            Model
                .builder()
                .services(
                    singletonList(
                        Service
                            .builder()
                            .host("petition.parliament.uk")
                            .port(80)
                            .endpoints(
                                singletonList(
                                    Endpoint
                                        .builder()
                                        .name("endpoint-0")
                                        .url("/petitions/131215.json")
                                        .topic("petitions/131215")
                                        .build()))
                            .build()))
                .build());

        LOG.debug("Configuration: {}", model);

        final PublishingClient diffusionClient = new PublishingClientImpl(model.getDiffusion().getHost(), model.getDiffusion().getPort());

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

        sleep(60000L);
        pollClient.stop();

        executor.shutdown();

        diffusionClient.stop();

        fileSystemPersistence.storeModel(model);
    }
}
