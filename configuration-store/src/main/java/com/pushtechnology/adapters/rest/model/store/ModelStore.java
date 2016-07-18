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

package com.pushtechnology.adapters.rest.model.store;

import java.util.function.Consumer;

import com.pushtechnology.adapters.rest.model.latest.Model;

/**
 * A store containing the runtime configuration model.
 *
 * @author Push Technology Limited
 */
public interface ModelStore {

    /**
     * @return the current model in the store
     */
    Model get();

    /**
     * Add a listener for changes to the model.
     */
    void onModelChange(Consumer<Model> newModel);
}
