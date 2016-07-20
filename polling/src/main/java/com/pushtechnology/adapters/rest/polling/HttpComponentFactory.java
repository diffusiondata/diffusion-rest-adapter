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

import static java.security.KeyStore.getDefaultType;
import static javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm;
import static javax.net.ssl.TrustManagerFactory.getInstance;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import com.pushtechnology.adapters.rest.model.latest.Model;

/**
 * Factory for {@link HttpComponent}.
 * @author Push Technology Limited
 */
public final class HttpComponentFactory {
    /**
     * @return a new {@link HttpComponent}
     */
    public HttpComponent create(Model model) {
        HttpAsyncClientBuilder builder = HttpAsyncClients
            .custom()
            .disableCookieManagement()
            .disableAuthCaching();

        if (model.getTruststore() != null) {
            builder = builder.setSSLContext(createTruststore(model));
        }

        final CloseableHttpAsyncClient client = builder.build();
        client.start();
        return new HttpComponentImpl(client);
    }

    private static SSLContext createTruststore(Model model) {
        final String truststoreLocation = model.getTruststore();
        try (InputStream stream = resolveTruststore(truststoreLocation)) {
            final KeyStore keyStore = KeyStore.getInstance(getDefaultType());
            keyStore.load(stream, null);
            final TrustManagerFactory trustManagerFactory = getInstance(getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        }
        catch (KeyStoreException |
            CertificateException |
            NoSuchAlgorithmException |
            IOException |
            KeyManagementException e) {

            throw new IllegalArgumentException("An SSLContext could not be created as requested in the" +
                " configuration for the HTTP client", e);
        }
    }

    private static InputStream resolveTruststore(String truststoreLocation) throws IOException {
        final InputStream stream = Thread
            .currentThread()
            .getContextClassLoader()
            .getResourceAsStream(truststoreLocation);

        if (stream == null) {
            return Files.newInputStream(Paths.get(truststoreLocation));
        }
        return stream;
    }
}
