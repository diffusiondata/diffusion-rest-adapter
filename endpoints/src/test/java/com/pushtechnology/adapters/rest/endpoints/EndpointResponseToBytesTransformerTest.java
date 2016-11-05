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

import static com.pushtechnology.adapters.rest.endpoints.EndpointResponseToBytesTransformer.INSTANCE;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.diffusion.transform.transformer.TransformationException;

/**
 * Unit tests for {@link EndpointResponseToBytesTransformer}.
 *
 * @author Push Technology Limited
 */
public final class EndpointResponseToBytesTransformerTest {
    public static final byte[] BYTES = new byte[0];
    @Mock
    private EndpointResponse endpointResponse;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void testTransformation() throws TransformationException, IOException {
        when(endpointResponse.getResponse()).thenReturn(BYTES);
        final byte[] value = INSTANCE.transform(endpointResponse);
        assertSame(BYTES, value);
    }

    @Test(expected = TransformationException.class)
    public void testException() throws TransformationException, IOException {
        doThrow(new IOException("Intentionally thrown by test")).when(endpointResponse).getResponse();
        INSTANCE.transform(endpointResponse);
    }
}
