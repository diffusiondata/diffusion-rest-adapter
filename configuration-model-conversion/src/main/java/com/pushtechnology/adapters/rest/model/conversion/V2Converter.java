package com.pushtechnology.adapters.rest.model.conversion;

import static java.util.stream.Collectors.toList;

import java.util.stream.Collectors;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.v3.Diffusion;
import com.pushtechnology.adapters.rest.model.v3.Endpoint;
import com.pushtechnology.adapters.rest.model.v3.Model;
import com.pushtechnology.adapters.rest.model.v3.Service;

/**
 * Converter between different version 2 of the model version 3.
 *
 * @author Push Technology Limited
 */
public enum V2Converter implements ModelConverter {
    INSTANCE;

    @Override
    public Model convert(AnyModel model) {
        if (model instanceof com.pushtechnology.adapters.rest.model.v2.Model) {
            final com.pushtechnology.adapters.rest.model.v2.Model oldModel =
                (com.pushtechnology.adapters.rest.model.v2.Model) model;

            return Model
                .builder()
                .services(oldModel
                    .getServices()
                    .stream()
                    .map(oldService -> Service
                        .builder()
                        .host(oldService.getHost())
                        .port(oldService.getPort())
                        .endpoints(oldService
                            .getEndpoints()
                            .stream()
                            .map(oldEndpoint -> Endpoint
                                .builder()
                                .name(oldEndpoint.getName())
                                .url(oldEndpoint.getUrl())
                                .topic(oldEndpoint.getTopic())
                                .build())
                            .collect(Collectors.toList()))
                        .pollPeriod(oldService.getPollPeriod())
                        .build())
                    .collect(toList()))
                .diffusion(Diffusion.builder().host("localhost").port(8080).build())
                .build();
        }
        else {
            throw new IllegalArgumentException("The argument " + model + " cannot be converted");
        }
    }

    @Override
    public ModelConverter next() {
        return V3Converter.INSTANCE;
    }
}
