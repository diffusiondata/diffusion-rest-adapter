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

import static com.pushtechnology.adapters.rest.endpoints.EndpointResponseToStringTransformer.INSTANCE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.polling.EndpointResponse;

/**
 * Unit tests for {@link EndpointResponseToStringTransformer}.
 *
 * @author Push Technology Limited
 */
public final class EndpointResponseToStringTransformerTest {

    @Mock
    private EndpointResponse endpointResponse;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void testTransformation() throws Exception {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain; charset=UTF-8");
        when(endpointResponse.getResponse()).thenReturn("{\"foo\":\"bar\"}".getBytes("UTF-8"));
        final String value = INSTANCE.transform(endpointResponse);
        assertEquals("{\"foo\":\"bar\"}", value);
    }

    @Test
    public void testTransformationCharsetMissing() throws Exception {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain");
        when(endpointResponse.getResponse()).thenReturn("{\"foo\":\"bar\"}".getBytes("ISO-8859-1"));
        final String value = INSTANCE.transform(endpointResponse);
        assertEquals("{\"foo\":\"bar\"}", value);
    }

    @Test(expected = IOException.class)
    public void testException() throws Exception {
        doThrow(new IOException("Intentionally thrown by test")).when(endpointResponse).getResponse();
        INSTANCE.transform(endpointResponse);
    }
}
