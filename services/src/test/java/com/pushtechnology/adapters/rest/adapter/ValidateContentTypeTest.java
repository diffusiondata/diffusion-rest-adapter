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

package com.pushtechnology.adapters.rest.adapter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;

import kotlin.Pair;

/**
 * Unit tests for {@link ValidateContentType}.
 *
 * @author Push Technology Limited
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness=Strictness.LENIENT)
public final class ValidateContentTypeTest {
    @Mock
    private EndpointResponse endpointResponse;

    private EndpointConfig jsonEndpoint = EndpointConfig
        .builder()
        .name("endpoint")
        .topicPath("path")
        .url("url")
        .produces("application/json")
        .build();
    private EndpointConfig plainEndpoint = EndpointConfig
        .builder()
        .name("endpoint")
        .topicPath("path")
        .url("url")
        .produces("text/plain")
        .build();
    private EndpointConfig binaryEndpoint = EndpointConfig
        .builder()
        .name("endpoint")
        .topicPath("path")
        .url("url")
        .produces("binary")
        .build();

    private ValidateContentType validate;

    @BeforeEach
    public void setUp() {
        when(endpointResponse.getContentType()).thenCallRealMethod();

        validate = new ValidateContentType();
    }

    @AfterEach
    public void postConditions() {
        verifyNoMoreInteractions(endpointResponse);
    }

    @Test
    public void unknownContentTypeJson() {
        validate.apply(new Pair<>(jsonEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
        verify(endpointResponse).getContentType();
    }

    @Test
    public void unknownContentTypePlain() {
        validate.apply(new Pair<>(plainEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
        verify(endpointResponse).getContentType();
    }

    @Test
    public void unknownContentTypeBinary() {
        validate.apply(new Pair<>(binaryEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
        verify(endpointResponse).getContentType();
    }

    @Test
    public void jsonContentTypeJson() {
        when(endpointResponse.getHeader("content-type")).thenReturn("application/json");
        validate.apply(new Pair<>(jsonEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
        verify(endpointResponse).getContentType();
    }

    @Test
    public void jsonContentTypePlain() {
        when(endpointResponse.getHeader("content-type")).thenReturn("application/json");
        validate.apply(new Pair<>(plainEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
        verify(endpointResponse).getContentType();
    }

    @Test
    public void jsonContentTypeBinary() {
        when(endpointResponse.getHeader("content-type")).thenReturn("application/json");
        validate.apply(new Pair<>(binaryEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
        verify(endpointResponse).getContentType();
    }

    @Test
    public void plainContentTypeJson() {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain");
        assertThrows(IllegalArgumentException.class, () -> {
            validate.apply(new Pair<>(jsonEndpoint, endpointResponse));
        });

        verify(endpointResponse).getHeader("content-type");
        verify(endpointResponse).getContentType();
    }

    @Test
    public void plainContentTypePlain() {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain");
        validate.apply(new Pair<>(plainEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
        verify(endpointResponse).getContentType();
    }

    @Test
    public void plainContentTypeBinary() {
        when(endpointResponse.getHeader("content-type")).thenReturn("text/plain");
        validate.apply(new Pair<>(binaryEndpoint, endpointResponse));

        verify(endpointResponse).getHeader("content-type");
        verify(endpointResponse).getContentType();
    }
}
