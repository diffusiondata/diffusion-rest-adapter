/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.pushtechnology.adapters.rest.polling.EndpointResponse;

/**
 * Unit tests for {@link EndpointResponseToStringTransformer}.
 *
 * @author Push Technology Limited
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness= Strictness.LENIENT)
public final class EndpointResponseToStringTransformerTest {

    @Mock
    private EndpointResponse endpointResponse;

    @BeforeEach
    public void setUp() {
        when(endpointResponse.getContentType()).thenCallRealMethod();
    }

    @Test
    public void testTransformation() throws Exception {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain; charset=UTF-8");
        when(endpointResponse.getResponse()).thenReturn("{\"foo\":\"bar\"}".getBytes(StandardCharsets.UTF_8));
        final String value = INSTANCE.transform(endpointResponse);
        assertEquals("{\"foo\":\"bar\"}", value);
    }

    @Test
    public void testTransformationCharsetMissing() throws Exception {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain");
        when(endpointResponse.getResponse()).thenReturn("{\"foo\":\"bar\"}".getBytes(StandardCharsets.ISO_8859_1));
        final String value = INSTANCE.transform(endpointResponse);
        assertEquals("{\"foo\":\"bar\"}", value);
    }

    @Test
    public void testException() {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain; charset=badcharset");
        when(endpointResponse.getResponse()).thenReturn("{\"foo\":\"bar\"}".getBytes(StandardCharsets.UTF_8));
        assertThrows(UnsupportedCharsetException.class, () -> {
            INSTANCE.transform(endpointResponse);
        });
    }
}
