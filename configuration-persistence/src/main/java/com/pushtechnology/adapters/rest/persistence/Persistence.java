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

package com.pushtechnology.adapters.rest.persistence;

import java.io.IOException;
import java.util.Optional;

import com.pushtechnology.adapters.rest.model.latest.Model;

/**
 * Allow the configuration model to be loaded and stored to a persisted store.
 *
 * @author Push Technology Limited
 */
public interface Persistence {
    /**
     * Attempt to load the model from a persisted store.
     * @return Empty or the model
     * @throws IOException if there was a problem with the store
     */
    Optional<Model> loadModel() throws IOException;

    /**
     * Attempt to store the model in a persisted store.
     * @param model the model to store
     * @throws IOException if there was a problem with the store
     */
    void storeModel(Model model) throws IOException;
}
