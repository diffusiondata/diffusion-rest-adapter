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

import static com.pushtechnology.adapters.rest.model.conversion.ConversionContext.FULL_CONTEXT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.latest.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.session.SessionAttributes;

/**
 * Unit tests for {@link ConversionContext}.
 *
 * @author Push Technology Limited
 */
public final class ConversionContextTest {

    @Test
    public void testConvertFromV11() {
        final Model model = FULL_CONTEXT.convert(com.pushtechnology.adapters.rest.model.v11.Model
            .builder()
            .active(false)
            .diffusion(com.pushtechnology.adapters.rest.model.v11.DiffusionConfig
                .builder()
                .host("example.com")
                .build())
            .services(Collections.emptyList()).build());

        assertFalse(model.isActive());
        assertEquals(0, model.getServices().size());
    }

    @Test
    public void testConvertFromV11WithService() {
        final Model model = FULL_CONTEXT.convert(com.pushtechnology.adapters.rest.model.v11.Model
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
        assertEquals("a", service.getTopicPathRoot());

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
        assertEquals("topic", endpoints.get(0).getTopicPath());
        assertEquals("/url", endpoints.get(0).getUrl());
        assertEquals("binary", endpoints.get(0).getProduces());
        assertNotNull(basic);
        assertEquals("control", basic.getUserid());
        assertEquals("password", basic.getPassword());
    }

    @Test
    public void testUnknownModel() {
        assertThrows(IllegalArgumentException.class, () -> {
            FULL_CONTEXT.convert(new AnyModel() { });
        });
    }

    @Test
    public void testUnknownModelVersion() {
        assertThrows(IllegalArgumentException.class, () -> {
            FULL_CONTEXT.modelVersion(4);
        });
    }

    @Test
    public void testLatestVersionIsFound() {
        assertEquals(Model.class, FULL_CONTEXT.modelVersion(Model.VERSION));
    }
}
