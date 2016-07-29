package com.pushtechnology.adapters.rest.model.conversion;

import static com.pushtechnology.adapters.rest.model.conversion.V8Converter.INSTANCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.pushtechnology.adapters.rest.model.v9.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.v9.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.v9.EndpointConfig;
import com.pushtechnology.adapters.rest.model.v9.Model;
import com.pushtechnology.adapters.rest.model.v9.SecurityConfig;
import com.pushtechnology.adapters.rest.model.v9.ServiceConfig;

/**
 * Unit tests for {@link V8Converter}.
 *
 * @author Push Technology Limited
 */
public final class V8ConverterTest {
    @Test
    public void testConvert() {
        final Model model = INSTANCE.convert(
            com.pushtechnology.adapters.rest.model.v8.Model
                .builder()
                .services(Collections.singletonList(
                    com.pushtechnology.adapters.rest.model.v8.ServiceConfig
                        .builder()
                        .host("localhost")
                        .port(80)
                        .endpoints(Collections.singletonList(com.pushtechnology.adapters.rest.model.v8.EndpointConfig
                            .builder()
                            .name("endpoint")
                            .topic("topic")
                            .url("/url")
                            .build()))
                        .pollPeriod(5000)
                        .topicRoot("a")
                        .build()
                ))
                .diffusion(com.pushtechnology.adapters.rest.model.v8.DiffusionConfig
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

        assertEquals("localhost", service.getHost());
        assertEquals(80, service.getPort());
        assertEquals(1, endpoints.size());
        assertEquals(5000, service.getPollPeriod());
        assertEquals("a", service.getTopicRoot());
        assertEquals(SecurityConfig.builder().build(), service.getSecurity());
        assertEquals("localhost", diffusion.getHost());
        assertEquals(8080, diffusion.getPort());
        assertEquals("control", model.getDiffusion().getPrincipal());
        assertEquals("password", model.getDiffusion().getPassword());
        assertEquals("endpoint", endpoints.get(0).getName());
        assertEquals("topic", endpoints.get(0).getTopic());
        assertEquals("/url", endpoints.get(0).getUrl());
        assertNull(basic);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownModel() {
        final ModelConverter converter = V3Converter.INSTANCE;

        converter.convert(com.pushtechnology.adapters.rest.model.v1.Model
            .builder()
            .services(Collections.<com.pushtechnology.adapters.rest.model.v1.ServiceConfig>emptyList())
            .build());
    }
}
