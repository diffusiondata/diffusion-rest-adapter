package com.pushtechnology.adapters.rest.model.conversion;

import java.util.Collections;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.v1.Model;
import com.pushtechnology.adapters.rest.model.v1.Service;

/**
 * Converter between different version 0 of the model and version 1.
 *
 * @author Push Technology Limited
 */
public enum V0Converter implements ModelConverter {
    INSTANCE;

    @Override
    public Model convert(AnyModel model) {
        if (model instanceof com.pushtechnology.adapters.rest.model.v0.Model) {
            return Model
                .builder()
                .services(Collections.<Service>emptyList())
                .build();
        }
        else {
            throw new IllegalArgumentException("The argument " + model + " cannot be converted");
        }
    }

    @Override
    public ModelConverter next() {
        return V1Converter.INSTANCE;
    }
}
