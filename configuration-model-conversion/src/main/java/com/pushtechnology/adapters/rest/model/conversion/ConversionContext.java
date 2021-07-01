/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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
import com.pushtechnology.adapters.rest.model.conversion.v11.V11Converter;
import com.pushtechnology.adapters.rest.model.conversion.v12.V12Converter;
import com.pushtechnology.adapters.rest.model.conversion.v13.V13Converter;
import com.pushtechnology.adapters.rest.model.conversion.v14.V14Converter;
import com.pushtechnology.adapters.rest.model.conversion.v15.V15Converter;
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
            com.pushtechnology.adapters.rest.model.v11.Model.VERSION,
            com.pushtechnology.adapters.rest.model.v11.Model.class,
            V11Converter.INSTANCE)
        .register(
            com.pushtechnology.adapters.rest.model.v12.Model.VERSION,
            com.pushtechnology.adapters.rest.model.v12.Model.class,
            V12Converter.INSTANCE)
        .register(
            com.pushtechnology.adapters.rest.model.v13.Model.VERSION,
            com.pushtechnology.adapters.rest.model.v13.Model.class,
            V13Converter.INSTANCE)
        .register(
            com.pushtechnology.adapters.rest.model.v14.Model.VERSION,
            com.pushtechnology.adapters.rest.model.v14.Model.class,
            V14Converter.INSTANCE)
        .register(
            com.pushtechnology.adapters.rest.model.v15.Model.VERSION,
            com.pushtechnology.adapters.rest.model.v15.Model.class,
            V15Converter.INSTANCE)
        .register(
            Model.VERSION,
            Model.class)
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
        ModelConverter currentConverter = converters.get(model.getClass());
        AnyModel currentModel = model;
        while (currentConverter != null) {
            // Convert the model
            currentModel = currentConverter.convert(currentModel);

            // Lookup the next converter to use
            currentConverter = converters.get(currentModel.getClass());
        }

        if (!(currentModel instanceof Model)) {
            throw new IllegalArgumentException(
                "The model " +
                model +
                " cannot be converted. There are no converters that can handle " +
                currentModel.getClass());
        }

        return (Model) currentModel;
    }

    /**
     * @param version the schema version number
     * @return the model class used by the version
     * @throws IllegalArgumentException if the version can't be found
     */
    public Class<? extends AnyModel> modelVersion(int version) {
        final Class<? extends AnyModel> modelClass = modelVersions.get(version);
        if (modelClass != null) {
            return modelClass;
        }
        else {
            throw new IllegalArgumentException("Unknown model version " + version);
        }
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
         * @param fromClass the model class for the version
         * @param converter a converter to another version
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
         * Register a model version with the context.
         * @param version the schema version
         * @param fromClass the model class for the version
         * @return the builder
         */
        public Builder register(int version, Class<? extends AnyModel> fromClass) {
            final Map<Integer, Class<? extends AnyModel>> newModelVersions = new HashMap<>();
            newModelVersions.putAll(modelVersions);
            newModelVersions.put(version, fromClass);
            return new Builder(newModelVersions, converters);
        }

        /**
         * @return a new {@link ConversionContext}
         */
        public ConversionContext build() {
            return new ConversionContext(modelVersions, converters);
        }
    }
}
