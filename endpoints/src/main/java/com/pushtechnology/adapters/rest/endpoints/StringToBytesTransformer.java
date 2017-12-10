/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.content.ContentFactory;
import com.pushtechnology.diffusion.datatype.Bytes;
import com.pushtechnology.diffusion.datatype.DataType;
import com.pushtechnology.diffusion.transform.transformer.UnsafeTransformer;

/**
 * Transformer from {@link String} to {@link Bytes}.
 *
 * @author Push Technology Limited
 */
/*package*/ final class StringToBytesTransformer implements UnsafeTransformer<String, Bytes> {
    /**
     * Instance of the transformer.
     */
    /*package*/ static final UnsafeTransformer<String, Bytes> STRING_TO_BYTES = new StringToBytesTransformer();
    private static final DataType<String> STRING_DATA_TYPE = Diffusion.dataTypes().string();
    private static final ContentFactory CONTENT = Diffusion.content();

    private StringToBytesTransformer() {
    }

    @Override
    public Bytes transform(String value) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(value.length());
        STRING_DATA_TYPE.writeValue(value, buffer);
        return CONTENT.newContent(buffer.toByteArray());
    }
}
