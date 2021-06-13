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

import static com.pushtechnology.adapters.rest.model.conversion.v13.V13Converter.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.pushtechnology.adapters.rest.model.conversion.v13.V13Converter;
import com.pushtechnology.adapters.rest.model.latest.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.v14.EndpointConfig;
import com.pushtechnology.adapters.rest.model.v14.Model;
import com.pushtechnology.adapters.rest.model.v14.ServiceConfig;

/**
 * Unit tests for {@link V13Converter}.
 *
 * @author Push Technology Limited
 */
public final class V13ConverterTest {
    @Test
    public void testConvert() {
        final Model model = INSTANCE.convert(
            com.pushtechnology.adapters.rest.model.v13.Model
                .builder()
                .active(true)
                .services(Collections.singletonList(
                    com.pushtechnology.adapters.rest.model.v13.ServiceConfig
                        .builder()
                        .name("service")
                        .host("localhost")
                        .port(80)
                        .endpoints(Collections.singletonList(com.pushtechnology.adapters.rest.model.v14.EndpointConfig
                            .builder()
                            .name("endpoint")
                            .topicPath("topic")
                            .url("/url")
                            .produces("binary")
                            .build()))
                        .pollPeriod(5000)
                        .topicPathRoot("a")
                        .security(com.pushtechnology.adapters.rest.model.latest.SecurityConfig
                            .builder()
                            .basic(BasicAuthenticationConfig
                                .builder()
                                .userid("control")
                                .password("password")
                                .build())
                            .build())
                        .build()
                ))
                .diffusion(com.pushtechnology.adapters.rest.model.v13.DiffusionConfig
                    .builder()
                    .host("localhost")
                    .port(8080)
                    .principal("control")
                    .password("password")
                    .connectionTimeout(500)
                    .reconnectionTimeout(5000)
                    .maximumMessageSize(64000)
                    .build())
                .build());

        assertEquals(1, model.getServices().size());
        final DiffusionConfig diffusion = model.getDiffusion();
        final ServiceConfig service = model.getServices().get(0);
        final List<EndpointConfig> endpoints = service.getEndpoints();
        final BasicAuthenticationConfig basic = service.getSecurity().getBasic();

        assertTrue(model.isActive());
        assertEquals("service", service.getName());
        assertEquals("localhost", service.getHost());
        assertEquals(80, service.getPort());
        assertEquals(1, endpoints.size());
        assertEquals(5000, service.getPollPeriod());
        assertEquals("a", service.getTopicPathRoot());

        assertEquals("localhost", diffusion.getHost());
        assertEquals(8080, diffusion.getPort());
        assertEquals("control", diffusion.getPrincipal());
        assertEquals("password", diffusion.getPassword());
        assertEquals(500, diffusion.getConnectionTimeout());
        assertEquals(5000, diffusion.getReconnectionTimeout());
        assertEquals(64000, diffusion.getMaximumMessageSize());

        assertEquals("endpoint", endpoints.get(0).getName());
        assertEquals("topic", endpoints.get(0).getTopicPath());
        assertEquals("/url", endpoints.get(0).getUrl());
        assertEquals("binary", endpoints.get(0).getProduces());
        assertNotNull(basic);
        assertEquals("control", basic.getUserid());
        assertEquals("password", basic.getPassword());

        assertNull(model.getMetrics().getSummary());
        assertFalse(model.getMetrics().isCounting());
    }

    @Test
    public void testConvertWithoutSecurity() {
        final Model model = INSTANCE.convert(
            com.pushtechnology.adapters.rest.model.v13.Model
                .builder()
                .active(true)
                .services(Collections.singletonList(
                    com.pushtechnology.adapters.rest.model.v13.ServiceConfig
                        .builder()
                        .name("service")
                        .host("localhost")
                        .port(80)
                        .endpoints(Collections.singletonList(com.pushtechnology.adapters.rest.model.v14.EndpointConfig
                            .builder()
                            .name("endpoint")
                            .topicPath("topic")
                            .url("/url")
                            .produces("binary")
                            .build()))
                        .pollPeriod(5000)
                        .topicPathRoot("a")
                        .security(null)
                        .build()
                ))
                .diffusion(com.pushtechnology.adapters.rest.model.v13.DiffusionConfig
                    .builder()
                    .host("localhost")
                    .port(8080)
                    .principal("control")
                    .password("password")
                    .connectionTimeout(500)
                    .reconnectionTimeout(5000)
                    .maximumMessageSize(64000)
                    .build())
                .build());

        assertEquals(1, model.getServices().size());
        final DiffusionConfig diffusion = model.getDiffusion();
        final ServiceConfig service = model.getServices().get(0);
        final List<EndpointConfig> endpoints = service.getEndpoints();
        final BasicAuthenticationConfig basic = service.getSecurity().getBasic();

        assertTrue(model.isActive());
        assertEquals("service", service.getName());
        assertEquals("localhost", service.getHost());
        assertEquals(80, service.getPort());
        assertEquals(1, endpoints.size());
        assertEquals(5000, service.getPollPeriod());
        assertEquals("a", service.getTopicPathRoot());

        assertEquals("localhost", diffusion.getHost());
        assertEquals(8080, diffusion.getPort());
        assertEquals("control", diffusion.getPrincipal());
        assertEquals("password", diffusion.getPassword());
        assertEquals(500, diffusion.getConnectionTimeout());
        assertEquals(5000, diffusion.getReconnectionTimeout());
        assertEquals(64000, diffusion.getMaximumMessageSize());

        assertEquals("endpoint", endpoints.get(0).getName());
        assertEquals("topic", endpoints.get(0).getTopicPath());
        assertEquals("/url", endpoints.get(0).getUrl());
        assertEquals("binary", endpoints.get(0).getProduces());
        assertNull(basic);

        assertNull(model.getMetrics().getSummary());
        assertFalse(model.getMetrics().isCounting());
    }

    @Test
    public void testUnknownModel() {
        final ModelConverter converter = V13Converter.INSTANCE;

        assertThrows(IllegalArgumentException.class, () -> {
            converter.convert(com.pushtechnology.adapters.rest.model.v11.Model
                .builder()
                .services(Collections.emptyList())
                .build());
        });
    }
}
