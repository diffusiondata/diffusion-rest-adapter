package com.pushtechnology.adapters.rest.adapter;

import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.apache.http.concurrent.FutureCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link ParsingHandlerFactory}.
 *
 * @author Push Technology Limited
 */
public final class ParsingHandlerFactoryTest {
    @Mock
    private FutureCallback<JSON> jsonHandler;
    @Mock
    private FutureCallback<Binary> binaryHandler;
    @Mock
    private FutureCallback<String> stringHandler;
    @Mock
    private FutureCallback<Integer> integerHandler;

    private ParsingHandlerFactory factory = new ParsingHandlerFactory();

    @Before
    public void setUp() {
        initMocks(this);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(jsonHandler, binaryHandler, stringHandler, integerHandler);
    }

    @Test
    public void createJson() {
        factory.create(JSON.class, jsonHandler);
    }

    @Test
    public void createBinary() {
        factory.create(Binary.class, binaryHandler);
    }

    @Test
    public void createPlainText() {
        factory.create(String.class, stringHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createInteger() {
        factory.create(Integer.class, integerHandler);
    }
}
