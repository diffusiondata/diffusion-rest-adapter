package com.pushtechnology.adapters.rest.model.conversion;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.v2.Model;

/**
 * Converter between different version 1 of the model and the latest.
 *
 * @author Push Technology Limited
 */
public enum V2Converter implements ModelConverter {
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
}
