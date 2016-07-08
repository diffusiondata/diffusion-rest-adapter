package com.pushtechnology.adapters.rest.model.conversion;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.pushtechnology.adapters.rest.model.v2.Model;
import com.pushtechnology.adapters.rest.model.v2.Service;

/**
 * Unit tests for {@link V0Converter}.
 *
 * @author Push Technology Limited
 */
public final class V0ConverterTest {

    @Test
    public void testConvert() {
        final ModelConverter converter = V0Converter.INSTANCE;

        final Model model = converter.convert(com.pushtechnology.adapters.rest.model.v0.Model.builder().build());

        assertEquals(0, model.getServices().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownModel() {
        final ModelConverter converter = V0Converter.INSTANCE;

        converter.convert(Model.builder().services(Collections.<Service>emptyList()).build());
    }
}
