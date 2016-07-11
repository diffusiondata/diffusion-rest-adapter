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

package com.pushtechnology.adapters.rest.model.conversion;

import static java.util.stream.Collectors.toList;

import java.util.stream.Collectors;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.latest.Diffusion;
import com.pushtechnology.adapters.rest.model.latest.Endpoint;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.Service;

/**
 * Converter between different version 3 of the model and version 4.
 *
 * @author Push Technology Limited
 */
public enum V3Converter implements ModelConverter {
    /**
     * The converter.
     */
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
