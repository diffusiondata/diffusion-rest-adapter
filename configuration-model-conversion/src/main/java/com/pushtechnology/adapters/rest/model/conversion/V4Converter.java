package com.pushtechnology.adapters.rest.model.conversion;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.v4.Model;

/**
 * Converter between different version 4 of the model and itself.
 *
 * @author Push Technology Limited
 */
public enum V4Converter implements ModelConverter {
    INSTANCE;

    @Override
    public Model convert(AnyModel model) {
        if (model instanceof Model) {
            return (Model) model;
        }
        else {
            throw new IllegalArgumentException("The argument " + model + " cannot be converted");
        }
    }

    @Override
    public ModelConverter next() {
        return null;
    }
}
