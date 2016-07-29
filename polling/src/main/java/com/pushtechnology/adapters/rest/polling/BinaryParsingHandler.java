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

import java.nio.charset.Charset;

import org.apache.http.concurrent.FutureCallback;

import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.datatype.binary.Binary;

/**
 * Handler that parses a response body as {@link Binary}.
 *
 * @author Push Technology Limited
 */
public final class BinaryParsingHandler implements FutureCallback<String> {
    private final FutureCallback<Binary> delegate;

    /**
     * Constructor.
     */
    public BinaryParsingHandler(FutureCallback<Binary> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void completed(String result) {
        delegate.completed(Diffusion.dataTypes().binary().readValue(result.getBytes(Charset.forName("UTF-8"))));
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
