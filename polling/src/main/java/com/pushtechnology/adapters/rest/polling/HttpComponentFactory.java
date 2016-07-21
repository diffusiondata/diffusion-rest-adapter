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

import javax.net.ssl.SSLContext;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;

/**
 * Factory for {@link HttpComponent}.
 * @author Push Technology Limited
 */
public final class HttpComponentFactory {
    /**
     * @return a new {@link HttpComponent}
     */
    public HttpComponent create(SSLContext sslContext) {
        HttpAsyncClientBuilder builder = HttpAsyncClients
            .custom()
            .disableCookieManagement()
            .disableAuthCaching();

        if (sslContext != null) {
            builder = builder.setSSLContext(sslContext);
        }

        final CloseableHttpAsyncClient client = builder.build();
        client.start();
        return new HttpComponentImpl(client);
    }
}
