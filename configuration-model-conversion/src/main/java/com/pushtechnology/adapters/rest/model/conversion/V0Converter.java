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

import java.util.Collections;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.v1.Model;
import com.pushtechnology.adapters.rest.model.v1.ServiceConfig;

import net.jcip.annotations.Immutable;

/**
 * Converter between different version 0 of the model and version 1.
 *
 * @author Push Technology Limited
 */
@Immutable
public enum V0Converter implements ModelConverter {
    /**
     * The converter.
     */
    INSTANCE;

    @Override
    public Model convert(AnyModel model) {
        if (model instanceof com.pushtechnology.adapters.rest.model.v0.Model) {
            return Model
                .builder()
                .services(Collections.<ServiceConfig>emptyList())
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
