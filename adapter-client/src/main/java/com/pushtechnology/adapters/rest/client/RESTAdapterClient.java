package com.pushtechnology.adapters.rest.client;

import static java.lang.Thread.sleep;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.pushtechnology.adapters.rest.model.conversion.ConversionContext;
import com.pushtechnology.adapters.rest.model.conversion.V0Converter;
import com.pushtechnology.adapters.rest.model.conversion.V1Converter;
import com.pushtechnology.adapters.rest.model.conversion.V2Converter;
import com.pushtechnology.adapters.rest.model.conversion.V3Converter;
import com.pushtechnology.adapters.rest.model.v3.Endpoint;
import com.pushtechnology.adapters.rest.model.v3.Model;
import com.pushtechnology.adapters.rest.model.v3.Service;
import com.pushtechnology.adapters.rest.persistence.FileSystemPersistence;
import com.pushtechnology.adapters.rest.persistence.Persistence;
import com.pushtechnology.adapters.rest.polling.PollClient;
import com.pushtechnology.adapters.rest.polling.ServiceSession;

/**
 * Simple client adapting REST to Diffusion.
 *
 * @author Push Technology Limited
 */
public class RESTAdapterClient {
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
                Model.VERSION,
                Model.class,
                V3Converter.INSTANCE)
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

        System.out.println(model);

        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        final PollClient pollClient = new PollClient();
        pollClient.start();

        for (Service service : model.getServices()) {
            final ServiceSession serviceSession = new ServiceSession(executor, pollClient, service);
            serviceSession.start();
        }

        sleep(10000L);
        pollClient.stop();

        executor.shutdown();

        fileSystemPersistence.storeModel(model);
    }
}
