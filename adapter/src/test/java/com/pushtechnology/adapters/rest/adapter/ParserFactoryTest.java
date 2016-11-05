package com.pushtechnology.adapters.rest.adapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.datatype.json.JSON;
import com.pushtechnology.diffusion.transform.transformer.TransformationException;
import com.pushtechnology.diffusion.transform.transformer.Transformer;

/**
 * Unit tests for {@link ParserFactory}.
 *
 * @author Push Technology Limited
 */
public final class ParserFactoryTest {
    @Mock
    private EndpointResponse endpointResponse;

    private ParserFactory factory = new ParserFactory();

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void createJson() throws IOException, TransformationException {
        when(endpointResponse.getResponse()).thenReturn("{\"key\":\"value\"}".getBytes());
        when(endpointResponse.getHeader("")).thenReturn("content-type:text/plain;charset=utf-8");
        final Transformer<EndpointResponse, JSON> parser = factory.create(JSON.class);
        final JSON value = parser.transform(endpointResponse);
        assertEquals(Diffusion.dataTypes().json().fromJsonString("{\"key\":\"value\"}"), value);
    }

    @Test
    public void createBinary() throws TransformationException, IOException {
        when(endpointResponse.getResponse()).thenReturn(new byte[0]);
        final Transformer<EndpointResponse, Binary> parser = factory.create(Binary.class);
        final Binary value = parser.transform(endpointResponse);
        assertEquals(0, value.length());
    }

    @Test
    public void createPlainText() {
        final Transformer<EndpointResponse, String> parser = factory.create(String.class);
        assertEquals(EndpointResponseToStringTransformer.INSTANCE, parser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createInteger() {
        factory.create(Integer.class);
    }
}
