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

package com.pushtechnology.adapters.rest.client;

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;
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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.publication.PublishingClientImpl;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClientImpl;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;

/**
 * Factory for {@link PublicationComponent}.
 *s
 * @author Push Technology Limited
 */
public final class PublicationComponentFactory {
    private static final Logger LOG = LoggerFactory.getLogger(PublicationComponentFactory.class);

    private final PollingComponentFactory pollingComponentFactory;

    /**
     * Constructor.
     */
    public PublicationComponentFactory(PollingComponentFactory pollingComponentFactory) {
        this.pollingComponentFactory = pollingComponentFactory;
    }

    /**
     * @return A new {@link PublicationComponent}
     */
    public PublicationComponent create(Model model, RESTAdapterClientCloseHandle client) {
        final AtomicBoolean isActive = new AtomicBoolean(true);
        final Session session = getSession(model.getDiffusion(), isActive, client);
        final TopicManagementClient topicManagementClient = new TopicManagementClientImpl(session);
        final PublishingClient publishingClient = new PublishingClientImpl(session);

        return new PublicationComponentImpl(
            isActive,
            session,
            topicManagementClient,
            publishingClient,
            pollingComponentFactory);
    }

    private static Session getSession(
        DiffusionConfig diffusionConfig,
        AtomicBoolean isActive,
        RESTAdapterClientCloseHandle client) {

        SessionFactory sessionFactory = Diffusion
            .sessions()
            .serverHost(diffusionConfig.getHost())
            .serverPort(diffusionConfig.getPort())
            .secureTransport(false)
            .transports(WEBSOCKET)
            .reconnectionTimeout(5000)
            .listener(new Listener(isActive, client));

        if (diffusionConfig.isSecure()) {
            sessionFactory = sessionFactory.secureTransport(true);

            if (diffusionConfig.getTruststore() != null) {
                final SSLContext sslContext = createTruststore(diffusionConfig);

                sessionFactory = sessionFactory.sslContext(sslContext);
            }
        }

        if (diffusionConfig.getPrincipal() != null && diffusionConfig.getPassword() != null) {
            sessionFactory = sessionFactory
                .principal(diffusionConfig.getPrincipal())
                .password(diffusionConfig.getPassword());
        }

        return sessionFactory.open();
    }

    private static SSLContext createTruststore(DiffusionConfig diffusionConfig) {
        try {
            final String truststoreLocation = diffusionConfig.getTruststore();
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(truststoreLocation);
            if (stream == null) {
                stream = Files.newInputStream(Paths.get(truststoreLocation));
            }

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
                " configuration");
        }
    }

    /**
     * A {@link Session.Listener} to handle session closes.
     * <p>
     * If the session closes when the state is active the connection and recovery must have failed, stop the client.
     */
    private static final class Listener implements Session.Listener {
        private final AtomicBoolean isActive;
        private final RESTAdapterClientCloseHandle client;

        public Listener(AtomicBoolean isActive, RESTAdapterClientCloseHandle client) {
            this.isActive = isActive;
            this.client = client;
        }

        @Override
        public void onSessionStateChanged(Session forSession, Session.State oldState, Session.State newState) {
            if (isActive.get() && newState.isClosed()) {
                try {
                    client.close();
                }
                catch (IOException e) {
                    LOG.warn("Exception stopping client", e);
                }
            }
        }
    }
}
