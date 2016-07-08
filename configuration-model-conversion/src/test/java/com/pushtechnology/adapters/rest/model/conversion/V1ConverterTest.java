package com.pushtechnology.adapters.rest.model.conversion;

import static com.pushtechnology.adapters.rest.model.conversion.V1Converter.INSTANCE;
import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.pushtechnology.adapters.rest.model.v2.Model;
import com.pushtechnology.adapters.rest.model.v2.Service;

/**
 * Unit tests for {@link V1Converter}.
 *
 * @author Push Technology Limited
 */
public final class V1ConverterTest {

    @Test
    public void testConvert() {
        final Model model = INSTANCE.convert(
            com.pushtechnology.adapters.rest.model.v1.Model
                .builder()
                .services(Collections.singletonList(
                    com.pushtechnology.adapters.rest.model.v1.Service
                    .builder()
                    .host("localhost")
                    .port(80)
                    .endpoints(Collections.<com.pushtechnology.adapters.rest.model.v1.Endpoint>emptyList())
                    .build()
                ))
                .build());

        assertEquals(1, model.getServices().size());
        final Service service = model.getServices().get(0);

        assertEquals("localhost", service.getHost());
        assertEquals(80, service.getPort());
        assertEquals(0, service.getEndpoints().size());
        assertEquals(60000, service.getPollPeriod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownModel() {
        final ModelConverter converter = INSTANCE;

        converter.convert(com.pushtechnology.adapters.rest.model.v0.Model.builder().build());
    }
}
