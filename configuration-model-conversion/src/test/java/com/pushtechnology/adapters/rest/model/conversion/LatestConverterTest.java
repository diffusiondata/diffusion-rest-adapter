package com.pushtechnology.adapters.rest.model.conversion;

import static org.junit.Assert.assertSame;

import java.util.Collections;

import org.junit.Test;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.latest.Model;

/**
 * Unit tests for {@link LatestConverter}.
 *
 * @author Push Technology Limited
 */
public final class LatestConverterTest {
    @Test
    public void testConvert() {
        final ModelConverter converter = LatestConverter.INSTANCE;

        final AnyModel anyModel = Model.builder().build();
        final AnyModel model = converter.convert(anyModel);

        assertSame(anyModel, model);
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