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

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import com.pushtechnology.adapters.rest.model.latest.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Implementation of {@link HttpClientFactory}.
 *
 * @author Push Technology Limited
 */
public final class HttpClientFactoryImpl implements HttpClientFactory {
    @Override
    public CloseableHttpAsyncClient create(Model model, SSLContext sslContext) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

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
            .disableCookieManagement()
            .setDefaultCredentialsProvider(credentialsProvider);

        if (sslContext != null) {
            builder = builder.setSSLContext(sslContext);
        }

        return builder.build();
    }

    private static AuthScope getAuthScope(ServiceConfig serviceConfig) {
        return new AuthScope(serviceConfig.getHost(), serviceConfig.getPort());
    }

    private static Credentials getCredentials(BasicAuthenticationConfig basicAuthenticationConfig) {
        return new UsernamePasswordCredentials(
            basicAuthenticationConfig.getPrincipal(),
            basicAuthenticationConfig.getCredential());
    }
}
