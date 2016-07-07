package com.pushtechnology.adapters.rest.model.conversion;

import java.util.HashMap;
import java.util.Map;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.v1.Model;

/**
 * Context for converting from older versions of the model.
 *
 * @author Push Technology Limited
 */
public final class ConversionContext implements ModelConverter {
    private final Map<Integer, Class<? extends AnyModel>> modelVersions;
    private final Map<Class<? extends AnyModel>, ModelConverter> converters;

    private ConversionContext(
            Map<Integer, Class<? extends AnyModel>> modelVersions,
            Map<Class<? extends AnyModel>, ModelConverter> converters) {
        this.modelVersions = modelVersions;
        this.converters = converters;
    }

    @Override
    public Model convert(AnyModel model) {
        final ModelConverter converter = converters.get(model.getClass());
        if (converter == null) {
            throw new IllegalArgumentException("The argument " + model + " cannot be converted");
        }

        return converter.convert(model);
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
            new HashMap<Integer, Class<? extends AnyModel>>(),
            new HashMap<Class<? extends AnyModel>, ModelConverter>());
    }

    /**
     * A builder for the {@link ConversionContext}
     */
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
