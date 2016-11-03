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
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.concurrent.FutureCallback;

import com.pushtechnology.adapters.rest.polling.EndpointResponse;

/**
 * Handler that parses a response body as {@link String}.
 *
 * @author Push Technology Limited
 */
public final class StringParsingHandler implements FutureCallback<EndpointResponse> {
    private static final Pattern CHARSET_PATTERN = Pattern.compile(".+; charset=(\\S+)");
    private final FutureCallback<String> delegate;

    /**
     * Constructor.
     */
    public StringParsingHandler(FutureCallback<String> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void completed(EndpointResponse response) {
        try {
            delegate.completed(new String(response.getResponse(), getResponseCharset(response)));
        }
        catch (IOException e) {
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
