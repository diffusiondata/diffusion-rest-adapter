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

import com.pushtechnology.adapters.rest.model.AnyModel;

/**
 * Abstract converter between different versions of the model.
 *
 * @param <M> the type of model this converter consumes
 * @param <N> the type of model this converter produces
 * @author Push Technology Limited
 */
public abstract class AbstractModelConverter<M extends AnyModel, N extends AnyModel> implements ModelConverter {
    private final ModelConverter nextConverter;
    private final Class<M> supportedModel;

    /**
     * Constructor.
     */
    protected AbstractModelConverter(ModelConverter nextConverter, Class<M> supportedModel) {
        this.nextConverter = nextConverter;
        this.supportedModel = supportedModel;
    }

    @Override
    public final N convert(AnyModel model) {
        if (model.getClass().equals(supportedModel)) {
            return convertFrom(supportedModel.cast(model));
        }
        else {
            throw new IllegalArgumentException("The argument " + model + " cannot be converted by " + this);
        }
    }

    /**
     * Convert a model from a specific version to a later version.
     * @param model The model
     * @return The model converted to a later version
     * @throws IllegalArgumentException if the converter does not know how to convert supplied model
     */
    protected abstract N convertFrom(M model);

    @Override
    public final ModelConverter next() {
        return nextConverter;
    }
}
