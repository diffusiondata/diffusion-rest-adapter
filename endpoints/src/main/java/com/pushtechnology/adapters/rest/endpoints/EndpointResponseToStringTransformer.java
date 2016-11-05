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

package com.pushtechnology.adapters.rest.endpoints;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.diffusion.transform.transformer.TransformationException;
import com.pushtechnology.diffusion.transform.transformer.Transformer;

/**
 * Transformer from {@link EndpointResponse} to {@link String}.
 *
 * @author Push Technology Limited
 */
/*package*/ final class EndpointResponseToStringTransformer implements Transformer<EndpointResponse, String> {
    private static final Pattern CHARSET_PATTERN = Pattern.compile(".+; charset=(\\S+)");
    /**
     * Instance of the transformer.
     */
    static final Transformer<EndpointResponse, String> INSTANCE = new EndpointResponseToStringTransformer();

    private EndpointResponseToStringTransformer() {
    }

    @Override
    public String transform(EndpointResponse response) throws TransformationException {
        try {
            return new String(response.getResponse(), getResponseCharset(response));
        }
        catch (IOException e) {
            throw new TransformationException(e);
        }
    }

    private Charset getResponseCharset(EndpointResponse response) {
        final String contentType = response.getHeader("content-type");
        if (contentType != null) {
            final Matcher matcher = CHARSET_PATTERN.matcher(contentType);

            if (matcher.matches()) {
                final String charset = matcher.group(1);
                return Charset.forName(charset);
            }
        }

        return Charset.forName("ISO-8859-1");
    }
}
