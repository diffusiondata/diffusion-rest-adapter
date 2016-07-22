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

import java.util.HashMap;
import java.util.Map;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.latest.Model;

import net.jcip.annotations.Immutable;

/**
 * Context for converting from older versions of the model.
 *
 * @author Push Technology Limited
 */
@Immutable
public final class ConversionContext implements ModelConverter {
    /**
     * A {@link ModelConverter} that can handle any type of model.
     */
    public static final ConversionContext FULL_CONTEXT = ConversionContext
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
            com.pushtechnology.adapters.rest.model.v4.Model.VERSION,
            com.pushtechnology.adapters.rest.model.v4.Model.class,
            V4Converter.INSTANCE)
        .register(
            com.pushtechnology.adapters.rest.model.v5.Model.VERSION,
            com.pushtechnology.adapters.rest.model.v5.Model.class,
            V5Converter.INSTANCE)
        .register(
            com.pushtechnology.adapters.rest.model.v6.Model.VERSION,
            com.pushtechnology.adapters.rest.model.v6.Model.class,
            V6Converter.INSTANCE)
        .register(
            com.pushtechnology.adapters.rest.model.v7.Model.VERSION,
            com.pushtechnology.adapters.rest.model.v7.Model.class,
            V7Converter.INSTANCE)
        .register(
            Model.VERSION,
            Model.class,
            LatestConverter.INSTANCE)
        .build();

    private final Map<Integer, Class<? extends AnyModel>> modelVersions = new HashMap<>();
    private final Map<Class<? extends AnyModel>, ModelConverter> converters = new HashMap<>();

    private ConversionContext(
            Map<Integer, Class<? extends AnyModel>> modelVersions,
            Map<Class<? extends AnyModel>, ModelConverter> converters) {

        this.modelVersions.putAll(modelVersions);
        this.converters.putAll(converters);
    }

    @Override
    public Model convert(AnyModel model) {
        final ModelConverter converter = converters.get(model.getClass());
        if (converter == null) {
            throw new IllegalArgumentException("The argument " + model + " cannot be converted");
        }

        return convertFrom(converter, model);
    }

    private Model convertFrom(ModelConverter converter, AnyModel model) {
        ModelConverter currentConverter = converter;
        AnyModel currentModel = model;
        while (currentConverter != null) {
            currentModel = currentConverter.convert(currentModel);
            currentConverter = currentConverter.next();
        }

        return (Model) currentModel;
    }

    @Override
    public ModelConverter next() {
        return null;
    }

    /**
     * @param version the schema version number
     * @return the model used by the version
     */
    public Class<? extends AnyModel> modelVersion(int version) {
        return modelVersions.get(version);
    }

    /**
     * @return {@link Builder} for the context
     */
    public static Builder builder() {
        return new Builder(
            new HashMap<>(),
            new HashMap<>());
    }

    /**
     * A builder for the {@link ConversionContext}.
     */
    @Immutable
    public static final class Builder {
        private final Map<Integer, Class<? extends AnyModel>> modelVersions;
        private final Map<Class<? extends AnyModel>, ModelConverter> converters;

        private Builder(
                Map<Integer, Class<? extends AnyModel>> modelVersions,
                Map<Class<? extends AnyModel>, ModelConverter> converters) {
            this.modelVersions = modelVersions;
            this.converters = converters;
        }

        /**
         * Register a conversion with the context.
         * @param version the schema version
         * @param fromClass the model for the version
         * @param converter the converter to the latest model
         * @return the builder
         */
        public Builder register(int version, Class<? extends AnyModel> fromClass, ModelConverter converter) {
            final Map<Class<? extends AnyModel>, ModelConverter> newConverters = new HashMap<>();
            newConverters.putAll(converters);
            newConverters.put(fromClass, converter);
            final Map<Integer, Class<? extends AnyModel>> newModelVersions = new HashMap<>();
            newModelVersions.putAll(modelVersions);
            newModelVersions.put(version, fromClass);
            return new Builder(newModelVersions, newConverters);
        }

        /**
         * @return a new {@link ConversionContext}
         */
        public ConversionContext build() {
            return new ConversionContext(modelVersions, converters);
        }
    }
}
