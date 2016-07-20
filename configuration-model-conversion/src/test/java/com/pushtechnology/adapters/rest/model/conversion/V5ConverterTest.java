package com.pushtechnology.adapters.rest.model.conversion;

import static com.pushtechnology.adapters.rest.model.conversion.V5Converter.INSTANCE;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.pushtechnology.adapters.rest.model.v6.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.v6.EndpointConfig;
import com.pushtechnology.adapters.rest.model.v6.Model;
import com.pushtechnology.adapters.rest.model.v6.ServiceConfig;

/**
 * Unit tests for {@link V5Converter}.
 *
 * @author Push Technology Limited
 */
public final class V5ConverterTest {
    @Test
    public void testConvert() {
        final Model model = INSTANCE.convert(
            com.pushtechnology.adapters.rest.model.v5.Model
                .builder()
                .services(singletonList(
                    com.pushtechnology.adapters.rest.model.v5.ServiceConfig
                        .builder()
                        .host("localhost")
                        .port(80)
                        .endpoints(singletonList(com.pushtechnology.adapters.rest.model.v5.EndpointConfig
                            .builder()
                            .name("endpoint")
                            .topic("topic")
                            .url("/url")
                            .build()))
                        .pollPeriod(5000)
                        .topicRoot("a")
                        .build()
                ))
                .diffusion(com.pushtechnology.adapters.rest.model.v5.DiffusionConfig
                    .builder()
                    .host("localhost")
                    .port(8080)
                    .build())
                .build());

        assertEquals(1, model.getServices().size());
        final DiffusionConfig diffusion = model.getDiffusion();
        final ServiceConfig service = model.getServices().get(0);
        final List<EndpointConfig> endpoints = service.getEndpoints();

        assertEquals("localhost", service.getHost());
        assertEquals(80, service.getPort());
        assertEquals(1, endpoints.size());
        assertEquals(5000, service.getPollPeriod());
        assertEquals("a", service.getTopicRoot());
        assertEquals("localhost", diffusion.getHost());
        assertEquals(8080, diffusion.getPort());
        assertEquals("endpoint", endpoints.get(0).getName());
        assertEquals("topic", endpoints.get(0).getTopic());
        assertEquals("/url", endpoints.get(0).getUrl());
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
