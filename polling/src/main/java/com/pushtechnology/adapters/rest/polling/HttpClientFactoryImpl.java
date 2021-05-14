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

package com.pushtechnology.adapters.rest.polling;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLContext;

import com.pushtechnology.adapters.rest.model.latest.Model;

/**
 * Implementation of {@link HttpClientFactory}.
 *
 * @author Push Technology Limited
 */
public final class HttpClientFactoryImpl implements HttpClientFactory {
    private final ThreadFactory threadFactory = new HttpClientThreadFactory();

    @Override
    public HttpClient create(Model model, SSLContext sslContext) {
        HttpClient.Builder builder = HttpClient
            .newBuilder()
            .cookieHandler(IgnoreCookies.handler())
            .authenticator(new SimpleAuthenticator(model))
            .executor(Executors.newFixedThreadPool(4, threadFactory));

        if (sslContext != null) {
            builder = builder.sslContext(sslContext);
        }

        return builder.build();
    }
}
