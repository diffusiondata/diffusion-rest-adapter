package com.pushtechnology.adapters.rest.model.conversion;

import static com.pushtechnology.adapters.rest.model.conversion.V5Converter.INSTANCE;
import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

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
                .services(Collections.singletonList(
                    com.pushtechnology.adapters.rest.model.v5.ServiceConfig
                        .builder()
                        .host("localhost")
                        .port(80)
                        .endpoints(Collections.<com.pushtechnology.adapters.rest.model.v5.EndpointConfig>emptyList())
                        .pollPeriod(5000)
                        .build()
                ))
                .diffusion(com.pushtechnology.adapters.rest.model.v5.DiffusionConfig
                    .builder()
                    .host("localhost")
                    .port(8080)
                    .build())
                .build());

        assertEquals(1, model.getServices().size());
        final ServiceConfig service = model.getServices().get(0);

        assertEquals("localhost", service.getHost());
        assertEquals(80, service.getPort());
        assertEquals(0, service.getEndpoints().size());
        assertEquals(5000, service.getPollPeriod());
        assertEquals("", service.getTopicRoot());
        assertEquals("localhost", model.getDiffusion().getHost());
        assertEquals(8080, model.getDiffusion().getPort());
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
