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

import java.io.IOException;

import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.diffusion.transform.transformer.TransformationException;
import com.pushtechnology.diffusion.transform.transformer.Transformer;

/**
 * Transformer from {@link EndpointResponse} to {@link byte[]}.
 *
 * @author Push Technology Limited
 */
/*package*/ final class EndpointResponseToBytesTransformer implements Transformer<EndpointResponse, byte[]> {
    /**
     * Instance of the transformer.
     */
    static final Transformer<EndpointResponse, byte[]> INSTANCE = new EndpointResponseToBytesTransformer();

    private EndpointResponseToBytesTransformer() {
    }

    @Override
    public byte[] transform(EndpointResponse endpointResponse) throws TransformationException {
        try {
            return endpointResponse.getResponse();
        }
        catch (IOException e) {
            throw new TransformationException(e);
        }
    }
}
