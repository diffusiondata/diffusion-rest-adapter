/*******************************************************************************
 * Copyright (C) 2016 Push Technology Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.pushtechnology.adapters.rest.polling;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.apache.http.concurrent.FutureCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.diffusion.datatype.binary.Binary;

/**
 * Unit tests for {@link BinaryParsingHandler}.
 *
 * @author Push Technology Limited
 */
public final class BinaryParsingHandlerTest {
    @Mock
    private FutureCallback<Binary> delegate;
    @Captor
    private ArgumentCaptor<Binary> binaryCaptor;

    private FutureCallback<String> handler;

    @Before
    public void setUp() {
        initMocks(this);

        handler = new BinaryParsingHandler(delegate);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void completed() {
        handler.completed("foobar");
        verify(delegate).completed(binaryCaptor.capture());

        final Binary binary = binaryCaptor.getValue();

        assertArrayEquals("foobar".getBytes(), binary.toByteArray());
    }

    @Test
    public void failed() {
        final Exception ex = new Exception("Intentional exception for test");
        handler.failed(ex);

        verify(delegate).failed(ex);
    }

    @Test
    public void cancelled() {
        handler.cancelled();

        verify(delegate).cancelled();
    }
}
