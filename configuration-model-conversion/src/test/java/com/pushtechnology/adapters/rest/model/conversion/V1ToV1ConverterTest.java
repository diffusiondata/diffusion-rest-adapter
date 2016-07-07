package com.pushtechnology.adapters.rest.model.conversion;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.pushtechnology.adapters.rest.model.v1.Model;
import com.pushtechnology.adapters.rest.model.v1.Service;

/**
 * Unit tests for {@link V1Converter}.
 *
 * @author Push Technology Limited
 */
public final class V1ToV1ConverterTest {

    @Test
    public void testConvert() {
        final ModelConverter converter = V1Converter.INSTANCE;

        final Model model = converter.convert(Model.builder().services(Collections.<Service>emptyList()).build());

        assertEquals(0, model.getServices().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownModel() {
        final ModelConverter converter = V1Converter.INSTANCE;

        converter.convert(com.pushtechnology.adapters.rest.model.v0.Model.builder().build());
    }
}
