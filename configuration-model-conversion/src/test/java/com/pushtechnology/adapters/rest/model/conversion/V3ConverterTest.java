package com.pushtechnology.adapters.rest.model.conversion;

import static com.pushtechnology.adapters.rest.model.conversion.V3Converter.INSTANCE;
import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.pushtechnology.adapters.rest.model.v4.Model;
import com.pushtechnology.adapters.rest.model.v4.Service;

/**
 * Unit tests for {@link V3Converter}.
 *
 * @author Push Technology Limited
 */
public final class V3ConverterTest {
    @Test
    public void testConvert() {
        final Model model = INSTANCE.convert(
            com.pushtechnology.adapters.rest.model.v3.Model
                .builder()
                .services(Collections.singletonList(
                    com.pushtechnology.adapters.rest.model.v3.Service
                        .builder()
                        .host("localhost")
                        .port(80)
                        .endpoints(Collections.<com.pushtechnology.adapters.rest.model.v3.Endpoint>emptyList())
                        .pollPeriod(5000)
                        .build()
                ))
                .diffusion(com.pushtechnology.adapters.rest.model.v3.Diffusion
                    .builder()
                    .host("localhost")
                    .port(8080)
                    .build())
                .build());

        assertEquals(1, model.getServices().size());
        final Service service = model.getServices().get(0);

        assertEquals("localhost", service.getHost());
        assertEquals(80, service.getPort());
        assertEquals(0, service.getEndpoints().size());
        assertEquals(5000, service.getPollPeriod());
        assertEquals("localhost", model.getDiffusion().getHost());
        assertEquals(8080, model.getDiffusion().getPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownModel() {
        final ModelConverter converter = V3Converter.INSTANCE;

        converter.convert(com.pushtechnology.adapters.rest.model.v1.Model
            .builder()
            .services(Collections.<com.pushtechnology.adapters.rest.model.v1.Service>emptyList())
            .build());
    }
}
