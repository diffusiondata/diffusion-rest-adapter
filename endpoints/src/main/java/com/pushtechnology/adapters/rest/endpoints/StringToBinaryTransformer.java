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

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;

import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.transform.transformer.ToBinaryTransformer;
import com.pushtechnology.diffusion.transform.transformer.TransformationException;
import com.pushtechnology.diffusion.transform.transformer.Transformer;

/**
 * Transformer from {@link String} to {@link Binary} using UTF-8 encoding.
 *
 * @author Push Technology Limited
 */
/*package*/ final class StringToBinaryTransformer extends ToBinaryTransformer<String> {
    /**
     * Instance of the transformer.
     */
    public static final Transformer<String, Binary> INSTANCE = new StringToBinaryTransformer();

    /**
     * Constructor.
     */
    private StringToBinaryTransformer() {
        super(64);
    }

    @Override
    protected void serialiseValue(DataOutput dataOutput, String value) throws TransformationException, IOException {
        dataOutput.write(value.getBytes(Charset.forName("UTF-8")));
    }
}
