package com.pushtechnology.adapters.rest.model.conversion;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.v1.Model;

/**
 * Converter between different versions of the model. Creates the latest model.
 *
 * @author Push Technology Limited
 */
public interface ModelConverter {
    /**
     * Convert a model to the latest version.
     * @param model The model
     * @return The model converted to the latest version
     * @throws IllegalArgumentException if the converter does not know how to convert supplied model
     */
    Model convert(AnyModel model);
}
