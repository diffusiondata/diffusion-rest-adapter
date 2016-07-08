package com.pushtechnology.adapters.rest.model.conversion;

import static com.pushtechnology.adapters.rest.model.conversion.V0Converter.INSTANCE;
import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.pushtechnology.adapters.rest.model.v1.Model;
import com.pushtechnology.adapters.rest.model.v1.Service;

/**
 * Unit tests for {@link V0Converter}.
 *
 * @author Push Technology Limited
 */
public final class V0ConverterTest {

    @Test
    public void testConvert() {
        final Model model = INSTANCE.convert(com.pushtechnology.adapters.rest.model.v0.Model.builder().build());

        assertEquals(0, model.getServices().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownModel() {
        final ModelConverter converter = INSTANCE;

        converter.convert(Model.builder().services(Collections.<Service>emptyList()).build());
    }
}
