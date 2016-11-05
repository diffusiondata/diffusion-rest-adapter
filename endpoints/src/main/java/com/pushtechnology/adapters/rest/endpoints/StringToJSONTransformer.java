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

package com.pushtechnology.adapters.rest.endpoints;

import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.datatype.InvalidDataException;
import com.pushtechnology.diffusion.datatype.json.JSON;
import com.pushtechnology.diffusion.transform.transformer.TransformationException;
import com.pushtechnology.diffusion.transform.transformer.Transformer;

/**
 * Transformer from {@link String} to {@link JSON}.
 *
 * @author Push Technology Limited
 */
/*package*/ final class StringToJSONTransformer implements Transformer<String, JSON> {
    /**
     * Instance of the transformer.
     */
    static final Transformer<String, JSON> INSTANCE = new StringToJSONTransformer();

    private StringToJSONTransformer() {
    }

    @Override
    public JSON transform(String value) throws TransformationException {
        try {
            return Diffusion.dataTypes().json().fromJsonString(value);
        }
        catch (InvalidDataException e) {
            throw new TransformationException(e);
        }
    }
}
