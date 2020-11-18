/*******************************************************************************
 * Copyright (C) 2020 Push Technology Ltd.
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.diffusion.transform.transformer.UnsafeTransformer;

/**
 * Transformer from {@link EndpointResponse} to {@link String}.
 *
 * @author Push Technology Limited
 */
/*package*/ final class EndpointResponseToStringTransformer implements UnsafeTransformer<EndpointResponse, String> {
    private static final Pattern CHARSET_PATTERN = Pattern.compile(".+; charset=(\\S+)");
    /**
     * Instance of the transformer.
     */
    static final UnsafeTransformer<EndpointResponse, String> INSTANCE = new EndpointResponseToStringTransformer();

    private EndpointResponseToStringTransformer() {
    }

    @Override
    public String transform(EndpointResponse response) {
        return new String(response.getResponse(), getResponseCharset(response));
    }

    private Charset getResponseCharset(EndpointResponse response) {
        final String contentType = response.getContentType();
        if (contentType != null) {
            final Matcher matcher = CHARSET_PATTERN.matcher(contentType);

            if (matcher.matches()) {
                final String charset = matcher.group(1);
                return Charset.forName(charset);
            }
        }

        return StandardCharsets.ISO_8859_1;
    }
}
