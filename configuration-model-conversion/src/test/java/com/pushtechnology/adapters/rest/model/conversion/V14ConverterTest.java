package com.pushtechnology.adapters.rest.model.conversion;

import static com.pushtechnology.adapters.rest.model.conversion.V14Converter.INSTANCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.pushtechnology.adapters.rest.model.latest.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.model.v14.SecurityConfig;

/**
 * Unit tests for {@link V14Converter}.
 *
 * @author Push Technology Limited
 */
public final class V14ConverterTest {
    @Test
    public void testConvert() {
        final Model model = INSTANCE.convert(
            com.pushtechnology.adapters.rest.model.v14.Model
                .builder()
                .services(Collections.singletonList(
                    com.pushtechnology.adapters.rest.model.v14.ServiceConfig
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
                        .security(SecurityConfig
                            .builder()
                            .basic(com.pushtechnology.adapters.rest.model.v14.BasicAuthenticationConfig
                                .builder()
                                .userid("control")
                                .password("password")
                                .build())
                            .build())
                        .build()
                ))
                .diffusion(com.pushtechnology.adapters.rest.model.v14.DiffusionConfig
                    .builder()
                    .host("localhost")
                    .port(8080)
                    .principal("control")
                    .password("password")
                    .connectionTimeout(500)
                    .reconnectionTimeout(5000)
                    .maximumMessageSize(64000)
                    .build())
                .metrics(com.pushtechnology.adapters.rest.model.v14.MetricsConfig.builder().counting(true).build())
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

        assertTrue(model.getMetrics().isCounting());
        assertNull(model.getMetrics().getPrometheus());
    }

    @Test
    public void testConvertWithoutSecurity() {
        final Model model = INSTANCE.convert(
            com.pushtechnology.adapters.rest.model.v14.Model
                .builder()
                .services(Collections.singletonList(
                    com.pushtechnology.adapters.rest.model.v14.ServiceConfig
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
                .diffusion(com.pushtechnology.adapters.rest.model.v14.DiffusionConfig
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

        assertFalse(model.getMetrics().isCounting());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownModel() {
        final ModelConverter converter = V13Converter.INSTANCE;

        converter.convert(com.pushtechnology.adapters.rest.model.v11.Model
            .builder()
            .services(Collections.emptyList())
            .build());
    }
}
