package com.pushtechnology.adapters.rest.model.conversion;

import static java.util.stream.Collectors.toList;

import java.util.stream.Collectors;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.v4.Diffusion;
import com.pushtechnology.adapters.rest.model.v4.Endpoint;
import com.pushtechnology.adapters.rest.model.v4.Model;
import com.pushtechnology.adapters.rest.model.v4.Service;

/**
 * Converter between different version 3 of the model and version 4.
 *
 * @author Push Technology Limited
 */
public enum V3Converter implements ModelConverter {
    INSTANCE;

    @Override
    public Model convert(AnyModel model) {
        if (model instanceof com.pushtechnology.adapters.rest.model.v3.Model) {
            final com.pushtechnology.adapters.rest.model.v3.Model oldModel =
                (com.pushtechnology.adapters.rest.model.v3.Model) model;

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
                .diffusion(Diffusion
                    .builder()
                    .host(oldModel.getDiffusion().getHost())
                    .port(oldModel.getDiffusion().getPort())
                    .build())
                .build();
        }
        else {
            throw new IllegalArgumentException("The argument " + model + " cannot be converted");
        }
    }

    @Override
    public ModelConverter next() {
        return V4Converter.INSTANCE;
    }
}
