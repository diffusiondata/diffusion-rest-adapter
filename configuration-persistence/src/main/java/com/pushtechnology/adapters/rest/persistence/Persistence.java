package com.pushtechnology.adapters.rest.persistence;

import java.io.IOException;
import java.util.Optional;

import com.pushtechnology.adapters.rest.model.v4.Model;

/**
 * Allow the configuration model to be loaded and stored to a persisted store.
 *
 * @author Push Technology Limited
 */
public interface Persistence {
    /**
     * Attempt to load the model from a persisted store
     * @return Empty or the model
     * @throws IOException if there was a problem with the store
     */
    Optional<Model> loadModel() throws IOException;

    /**
     * Attempt to store the model in a persisted store
     * @param model the model to store
     * @throws IOException if there was a problem with the store
     */
    void storeModel(Model model) throws IOException;
}
