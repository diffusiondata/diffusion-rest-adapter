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
 * Converter between different versions of the model.
 *
 * @author Push Technology Limited
 */
public interface ModelConverter {
    /**
     * Convert a model to a later.
     * @param model The model
     * @return The model converted to a later
     * @throws IllegalArgumentException if the converter does not know how to convert supplied model
     */
    AnyModel convert(AnyModel model);

    /**
     * @return The next converter that can be applied to the models returned by this converter
     */
    ModelConverter next();
}
