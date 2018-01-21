package com.pushtechnology.adapters.rest.model.conversion;

import static com.pushtechnology.adapters.rest.model.conversion.ConversionContext.FULL_CONTEXT;
import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.latest.Model;

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
            .diffusion(com.pushtechnology.adapters.rest.model.v11.DiffusionConfig
                .builder()
                .host("example.com")
                .build())
            .services(Collections.<com.pushtechnology.adapters.rest.model.v11.ServiceConfig>emptyList()).build());

        assertEquals(0, model.getServices().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownModel() {
        FULL_CONTEXT.convert(new AnyModel() { });
    }
}
