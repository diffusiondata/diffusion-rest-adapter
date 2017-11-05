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

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.apache.http.concurrent.FutureCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.diffusion.transform.transformer.TransformationException;

/**
 * Unit tests for {@link TransformingHandler}.
 *
 * @author Push Technology Limited
 */
public final class TransformingHandlerTest {
    @Mock
    private FutureCallback<String> delegate;

    private TransformingHandler<String, String> handler;

    @Before
    public void setUp() {
        initMocks(this);

        handler = new TransformingHandler<>(value -> value, delegate);
    }

    @Test
    public void completed() {
        handler.completed("a");

        verify(delegate).completed("a");
    }

    @Test
    public void failed() {
        final Exception e = new Exception("Intentionally created for test");

        handler.failed(e);

        verify(delegate).failed(e);
    }

    @Test
    public void cancelled() {
        handler.cancelled();

        verify(delegate).cancelled();
    }

    @Test
    public void completedThrowsException() {
        final TransformationException e = new TransformationException("Intentionally created for test");

        handler = new TransformingHandler<>(
            value -> {
                throw e;
            },
            delegate);

        handler.completed("a");

        verify(delegate).failed(e);
    }
}
