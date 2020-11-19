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

package com.pushtechnology.adapters.rest.polling;

import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLContext;


import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.nio.ssl.BasicClientTlsStrategy;

import com.pushtechnology.adapters.rest.model.latest.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Implementation of {@link HttpClientFactory}.
 *
 * @author Push Technology Limited
 */
public final class HttpClientFactoryImpl implements HttpClientFactory {
    private final ThreadFactory threadFactory = new HttpClientThreadFactory();

    @Override
    public CloseableHttpAsyncClient create(Model model, SSLContext sslContext) {
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        // Configure client with Basic authentication credentials
        model
            .getServices()
            .stream()
            .filter(ServiceConfig::isSecure)
            .filter(serviceConfig -> serviceConfig.getSecurity() != null)
            .filter(serviceConfig -> serviceConfig.getSecurity().getBasic() != null)
            .forEach(serviceConfig -> {
                final AuthScope authScope = getAuthScope(serviceConfig);
                final Credentials credentials = getCredentials(serviceConfig.getSecurity().getBasic());
                credentialsProvider.setCredentials(authScope, credentials);
            });

        HttpAsyncClientBuilder builder = HttpAsyncClients
            .custom()
            .setThreadFactory(threadFactory)
            .disableCookieManagement()
            .setDefaultCredentialsProvider(credentialsProvider);

        if (sslContext != null) {
            builder = builder.setConnectionManager(PoolingAsyncClientConnectionManagerBuilder
                .create()
                .setTlsStrategy(new BasicClientTlsStrategy(sslContext))
                .build());
        }

        return builder.build();
    }

    private static AuthScope getAuthScope(ServiceConfig serviceConfig) {
        return new AuthScope(serviceConfig.getHost(), serviceConfig.getPort());
    }

    private static Credentials getCredentials(BasicAuthenticationConfig basicAuthenticationConfig) {
        return new UsernamePasswordCredentials(
            basicAuthenticationConfig.getUserid(),
            basicAuthenticationConfig.getPassword().toCharArray());
    }

}
