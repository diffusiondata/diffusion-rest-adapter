/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

import java.util.function.BiConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.pushtechnology.diffusion.transform.transformer.TransformationException;

/**
 * Unit tests for {@link TransformingHandler}.
 *
 * @author Push Technology Limited
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness= Strictness.LENIENT)
public final class TransformingHandlerTest {
    @Mock
    private BiConsumer<String, Throwable> delegate;

    private TransformingHandler<String, String> handler;

    @BeforeEach
    public void setUp() {
        handler = new TransformingHandler<>(value -> value, delegate);
    }

    @Test
    public void completed() {
        handler.accept("a", null);

        verify(delegate).accept("a", null);
    }

    @Test
    public void failed() {
        final Exception e = new Exception("Intentionally created for test");

        handler.accept(null, e);

        verify(delegate).accept(null, e);
    }

    @Test
    public void completedThrowsException() {
        final TransformationException e = new TransformationException("Intentionally created for test");

        handler = new TransformingHandler<>(
            value -> {
                throw e;
            },
            delegate);

        handler.accept("a", null);

        verify(delegate).accept(null, e);
    }
}
