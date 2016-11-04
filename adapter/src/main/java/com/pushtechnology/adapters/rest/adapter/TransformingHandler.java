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

import org.apache.http.concurrent.FutureCallback;

import com.pushtechnology.diffusion.transform.transformer.TransformationException;
import com.pushtechnology.diffusion.transform.transformer.Transformer;

/**
 * A {@link FutureCallback} that applies a transformer and delegates to another {@link FutureCallback}.
 *
 * @param <S> the type to transform from
 * @param <T> the type to transform to
 * @author Push Technology Limited
 */
/*package*/ final class TransformingHandler<S, T> implements FutureCallback<S> {
    private final Transformer<S, T> transformer;
    private final FutureCallback<T> delegate;

    /**
     * Constructor.
     */
    public TransformingHandler(Transformer<S, T> transformer, FutureCallback<T> delegate) {
        this.transformer = transformer;
        this.delegate = delegate;
    }

    @Override
    public void completed(S result) {
        try {
            final T transformedResult = transformer.transform(result);
            delegate.completed(transformedResult);
        }
        catch (TransformationException e) {
            delegate.failed(e);
        }
    }

    @Override
    public void failed(Exception ex) {
        delegate.failed(ex);
    }

    @Override
    public void cancelled() {
        delegate.cancelled();
    }
}
