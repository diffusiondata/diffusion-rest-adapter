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

package com.pushtechnology.adapters.rest.model.conversion.v11;

import static java.util.stream.Collectors.toList;

import com.pushtechnology.adapters.rest.model.conversion.AbstractModelConverter;
import com.pushtechnology.adapters.rest.model.v12.Model;

import net.jcip.annotations.Immutable;

/**
 * Converter between different version 11 of the model and version 12.
 *
 * @author Push Technology Limited
 */
@Immutable
public final class V11Converter
        extends AbstractModelConverter<com.pushtechnology.adapters.rest.model.v11.Model, Model> {
    /**
     * The converter.
     */
    public static final V11Converter INSTANCE = new V11Converter();

    private V11Converter() {
        super(com.pushtechnology.adapters.rest.model.v11.Model.class);
    }

    @Override
    protected Model convertFrom(com.pushtechnology.adapters.rest.model.v11.Model model) {
        return Model
            .builder()
            .active(model.isActive())
            .services(model
                .getServices()
                .stream()
                .map(ServiceConfigConverter.INSTANCE::convert)
                .collect(toList()))
            .diffusion(DiffusionConfigConverter.INSTANCE.convert(model.getDiffusion()))
            .truststore(model.getTruststore())
            .build();
    }
}
