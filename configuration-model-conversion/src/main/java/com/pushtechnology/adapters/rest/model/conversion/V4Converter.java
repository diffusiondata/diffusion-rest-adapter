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

import com.pushtechnology.adapters.rest.model.v5.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.v5.EndpointConfig;
import com.pushtechnology.adapters.rest.model.v5.Model;
import com.pushtechnology.adapters.rest.model.v5.ServiceConfig;

import net.jcip.annotations.Immutable;

/**
 * Converter between different version 4 of the model and version 5.
 *
 * @author Push Technology Limited
 */
@Immutable
public final class V4Converter extends AbstractModelConverter<com.pushtechnology.adapters.rest.model.v4.Model, Model> {
    /**
     * The converter.
     */
    public static final V4Converter INSTANCE = new V4Converter();

    private V4Converter() {
        super(com.pushtechnology.adapters.rest.model.v4.Model.class);
    }

    @Override
    protected Model convertFrom(com.pushtechnology.adapters.rest.model.v4.Model model) {
        return Model
            .builder()
            .services(model
                .getServices()
                .stream()
                .map(oldService -> ServiceConfig
                    .builder()
                    .host(oldService.getHost())
                    .port(oldService.getPort())
                    .endpoints(oldService
                        .getEndpoints()
                        .stream()
                        .map(oldEndpoint -> EndpointConfig
                            .builder()
                            .name(oldEndpoint.getName())
                            .url(oldEndpoint.getUrl())
                            .topic(oldEndpoint.getTopic())
                            .build())
                        .collect(toList()))
                    .pollPeriod(oldService.getPollPeriod())
                    .topicRoot("")
                    .build())
                .collect(toList()))
            .diffusion(DiffusionConfig
                .builder()
                .host(model.getDiffusion().getHost())
                .port(model.getDiffusion().getPort())
                .principal(model.getDiffusion().getPrincipal())
                .password(model.getDiffusion().getPassword())
                .build())
            .build();
    }
}
