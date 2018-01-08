/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

import java.util.function.BiConsumer;

import com.pushtechnology.diffusion.transform.transformer.UnsafeTransformer;

/**
 * A {@link BiConsumer} that applies a transformer and delegates to another {@link BiConsumer}.
 *
 * @param <S> the type to transform from
 * @param <T> the type to transform to
 * @author Push Technology Limited
 */
/*package*/ final class TransformingHandler<S, T> implements BiConsumer<S, Throwable> {
    private final UnsafeTransformer<S, T> transformer;
    private final BiConsumer<T, Throwable> delegate;

    /**
     * Constructor.
     */
    public TransformingHandler(UnsafeTransformer<S, T> transformer, BiConsumer<T, Throwable> delegate) {
        this.transformer = transformer;
        this.delegate = delegate;
    }

    @Override
    public void accept(S result, Throwable throwable) {
        try {
            delegate.accept(result != null ? transformer.transform(result) : null, throwable);
        }
        // CHECKSTYLE.OFF: IllegalCatch
        catch (Exception e) {
            delegate.accept(null, e);
        }
        // CHECKSTYLE.ON: IllegalCatch
    }
}
