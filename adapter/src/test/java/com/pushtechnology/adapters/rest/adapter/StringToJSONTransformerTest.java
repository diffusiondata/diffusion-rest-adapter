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

package com.pushtechnology.adapters.rest.adapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.pushtechnology.diffusion.datatype.json.JSON;
import com.pushtechnology.diffusion.transform.transformer.TransformationException;

/**
 * Unit tests for {@link StringToJSONTransformer}.
 *
 * @author Push Technology Limited
 */
public final class StringToJSONTransformerTest {

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void testTransformation() throws TransformationException, IOException {
        final JSON value = StringToJSONTransformer.INSTANCE.transform("{\"foo\":\"bar\"}");
        assertEquals("{\"foo\":\"bar\"}", value.toJsonString());
    }

    @Test(expected = TransformationException.class)
    public void parsingFailure() throws TransformationException {
        StringToJSONTransformer.INSTANCE.transform("{\"foo\":\"");
    }
}
