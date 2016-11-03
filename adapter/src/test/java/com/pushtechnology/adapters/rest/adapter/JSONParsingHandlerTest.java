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

package com.pushtechnology.adapters.rest.adapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.isA;
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

import com.pushtechnology.diffusion.datatype.InvalidDataException;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link JSONParsingHandler}.
 *
 * @author Push Technology Limited
 */
public final class JSONParsingHandlerTest {
    @Mock
    private FutureCallback<JSON> delegate;
    @Captor
    private ArgumentCaptor<JSON> jsonCaptor;

    private FutureCallback<String> handler;

    @Before
    public void setUp() {
        initMocks(this);

        handler = new JSONParsingHandler(delegate);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void completed() {
        handler.completed("{\"foo\":\"bar\"}");
        verify(delegate).completed(jsonCaptor.capture());

        final JSON json = jsonCaptor.getValue();

        assertEquals("{\"foo\":\"bar\"}", json.toJsonString());
    }

    @Test
    public void parsingFailure() {
        handler.completed("{\"foo\":\"");
        verify(delegate).failed(isA(InvalidDataException.class));
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
