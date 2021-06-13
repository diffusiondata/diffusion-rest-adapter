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

package com.pushtechnology.adapters.rest.model.conversion;

import static com.pushtechnology.adapters.rest.model.conversion.v11.V11Converter.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.pushtechnology.adapters.rest.model.conversion.v11.V11Converter;
import com.pushtechnology.adapters.rest.model.v12.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.v12.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.v12.EndpointConfig;
import com.pushtechnology.adapters.rest.model.v12.Model;
import com.pushtechnology.adapters.rest.model.v12.ServiceConfig;
import com.pushtechnology.diffusion.client.session.SessionAttributes;

/**
 * Unit tests for {@link V11Converter}.
 *
 * @author Push Technology Limited
 */
public final class V11ConverterTest {
    @Test
    public void testConvert() {
        final Model model = INSTANCE.convert(
            com.pushtechnology.adapters.rest.model.v11.Model
                .builder()
                .active(true)
                .services(Collections.singletonList(
                    com.pushtechnology.adapters.rest.model.v11.ServiceConfig
                        .builder()
                        .host("localhost")
                        .port(80)
                        .endpoints(Collections.singletonList(com.pushtechnology.adapters.rest.model.v12.EndpointConfig
                            .builder()
                            .name("endpoint")
                            .topic("topic")
                            .url("/url")
                            .produces("binary")
                            .build()))
                        .pollPeriod(5000)
                        .topicRoot("a")
                        .security(com.pushtechnology.adapters.rest.model.v12.SecurityConfig
                            .builder()
                            .basic(com.pushtechnology.adapters.rest.model.v12.BasicAuthenticationConfig
                                .builder()
                                .principal("control")
                                .credential("password")
                                .build())
                            .build())
                        .build()
                ))
                .diffusion(com.pushtechnology.adapters.rest.model.v11.DiffusionConfig
                    .builder()
                    .host("localhost")
                    .port(8080)
                    .principal("control")
                    .password("password")
                    .build())
                .build());

        assertEquals(1, model.getServices().size());
        final DiffusionConfig diffusion = model.getDiffusion();
        final ServiceConfig service = model.getServices().get(0);
        final List<EndpointConfig> endpoints = service.getEndpoints();
        final BasicAuthenticationConfig basic = service.getSecurity().getBasic();

        assertTrue(model.isActive());
        assertEquals("localhost:80:false", service.getName());
        assertEquals("localhost", service.getHost());
        assertEquals(80, service.getPort());
        assertEquals(1, endpoints.size());
        assertEquals(5000, service.getPollPeriod());
        assertEquals("a", service.getTopicRoot());

        assertEquals("localhost", diffusion.getHost());
        assertEquals(8080, diffusion.getPort());
        assertEquals("control", diffusion.getPrincipal());
        assertEquals("password", diffusion.getPassword());
        assertEquals(SessionAttributes.DEFAULT_CONNECTION_TIMEOUT, diffusion.getConnectionTimeout());
        assertEquals(SessionAttributes.DEFAULT_RECONNECTION_TIMEOUT, diffusion.getReconnectionTimeout());
        assertEquals(SessionAttributes.DEFAULT_MAXIMUM_MESSAGE_SIZE, diffusion.getMaximumMessageSize());
        assertEquals(SessionAttributes.DEFAULT_INPUT_BUFFER_SIZE, diffusion.getInputBufferSize());
        assertEquals(SessionAttributes.DEFAULT_OUTPUT_BUFFER_SIZE, diffusion.getOutputBufferSize());
        assertEquals(SessionAttributes.DEFAULT_RECOVERY_BUFFER_SIZE, diffusion.getRecoveryBufferSize());

        assertEquals("endpoint", endpoints.get(0).getName());
        assertEquals("topic", endpoints.get(0).getTopic());
        assertEquals("/url", endpoints.get(0).getUrl());
        assertEquals("binary", endpoints.get(0).getProduces());
        assertNotNull(basic);
        assertEquals("control", basic.getPrincipal());
        assertEquals("password", basic.getCredential());
    }

    @Test
    public void testUnknownModel() {
        final ModelConverter converter = V11Converter.INSTANCE;

        assertThrows(IllegalArgumentException.class, () -> {
            converter.convert(com.pushtechnology.adapters.rest.model.v12.Model
                                  .builder()
                                  .services(Collections.emptyList())
                                  .build());
        });
    }
}
