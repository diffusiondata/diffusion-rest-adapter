package com.pushtechnology.adapters.rest.model.conversion;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Unit tests for {@link ConversionContext}.
 *
 * @author Push Technology Limited
 */
public final class ConversionContextTest {

    private ConversionContext converter;

    @Before
    public void setUp() {
        converter = ConversionContext
            .builder()
            .register(
                com.pushtechnology.adapters.rest.model.v0.Model.VERSION,
                com.pushtechnology.adapters.rest.model.v0.Model.class,
                V0Converter.INSTANCE)
            .register(
                com.pushtechnology.adapters.rest.model.v1.Model.VERSION,
                com.pushtechnology.adapters.rest.model.v1.Model.class,
                V1Converter.INSTANCE)
            .register(
                com.pushtechnology.adapters.rest.model.v2.Model.VERSION,
                com.pushtechnology.adapters.rest.model.v2.Model.class,
                V2Converter.INSTANCE)
            .register(
                com.pushtechnology.adapters.rest.model.v3.Model.VERSION,
                com.pushtechnology.adapters.rest.model.v3.Model.class,
                V3Converter.INSTANCE)
            .register(
                com.pushtechnology.adapters.rest.model.v4.Model.VERSION,
                com.pushtechnology.adapters.rest.model.v4.Model.class,
                V4Converter.INSTANCE)
            .register(
                com.pushtechnology.adapters.rest.model.v5.Model.VERSION,
                com.pushtechnology.adapters.rest.model.v5.Model.class,
                V5Converter.INSTANCE)
            .register(
                Model.VERSION,
                Model.class,
                LatestConverter.INSTANCE)
            .build();
    }

    @Test
    public void testConvertFromV1() {
        final Model model = converter.convert(Model.builder().services(Collections.<ServiceConfig>emptyList()).build());

        assertEquals(0, model.getServices().size());
    }

    @Test
    public void testConvertFromV0() {
        final Model model = converter.convert(com.pushtechnology.adapters.rest.model.v0.Model.builder().build());

        assertEquals(0, model.getServices().size());
    }

    @Test
    public void testModelVersion0() {
        assertEquals(com.pushtechnology.adapters.rest.model.v0.Model.class, converter.modelVersion(0));
    }

    @Test
    public void testModelVersion1() {
        assertEquals(Model.class, converter.modelVersion(Model.VERSION));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownModel() {
        converter.convert(new AnyModel() { });
    }
}
