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

package com.pushtechnology.adapters.rest.cloud.foundry.web;

import static java.security.KeyStore.getDefaultType;
import static javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm;
import static javax.net.ssl.TrustManagerFactory.getInstance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import com.pushtechnology.adapters.rest.cloud.foundry.vcap.ReapptCredentials;
import com.pushtechnology.adapters.rest.cloud.foundry.vcap.VCAP;

/**
 * Entry point for Diffusion Cloud Foundry REST Adapter Web Interface.
 *
 * @author Push Technology Limited
 */
public final class CloudFoundryWebInterface {
    private CloudFoundryWebInterface() {
    }

    /**
     * Entry point for Cloud Foundry REST Adapter.
     * @param args The command line arguments
     * @throws Exception it there was a problem starting the web interface
     */
    // CHECKSTYLE.OFF: UncommentedMain // Entry point for runnable JAR
    public static void main(String[] args) throws Exception {
        // CHECKSTYLE.ON: UncommentedMain

        final ReapptCredentials reapptCredentials = VCAP.getServices()
            .getReappt()
            .getCredentials();

        final SSLContext sslContext;
        try (InputStream stream = Thread
            .currentThread()
            .getContextClassLoader()
            .getResourceAsStream("reapptTruststore.jks")) {

            final KeyStore keyStore = KeyStore.getInstance(getDefaultType());
            keyStore.load(stream, null);
            final TrustManagerFactory trustManagerFactory = getInstance(getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        }
        catch (KeyStoreException |
            CertificateException |
            NoSuchAlgorithmException |
            IOException |
            KeyManagementException e) {

            throw new IllegalArgumentException("An SSLContext could not be created as requested in the" +
                " configuration for the Diffusion client", e);
        }

        final Path tempFile = Files.createTempFile("web-interface-servlet", ".war");
        extractWarTo(tempFile);

        final Server jettyServer = new Server(VCAP.getPort());
        final WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setInitParameter("host", reapptCredentials.getHost());
        webapp.setInitParameter("port", "443");
        webapp.setInitParameter("secure", "true");
        webapp.setWar(tempFile.toAbsolutePath().toString());
        final GzipHandler gzipHandler = new GzipHandler();
        gzipHandler.setHandler(webapp);
        jettyServer.setHandler(gzipHandler);

        jettyServer.start();
    }

    private static void extractWarTo(Path warFile) throws IOException {
        final URL servlet = Thread.currentThread().getContextClassLoader().getResource("web-interface-servlet.war");
        if (servlet == null) {
            throw new IllegalStateException("Web interface not located");
        }
        final InputStream inputStream = servlet.openStream();
        final OutputStream outputStream = Files.newOutputStream(warFile, StandardOpenOption.TRUNCATE_EXISTING);

        // Copy resource to temporary file
        do {
            final int nextByte = inputStream.read();
            if (nextByte != -1) {
                outputStream.write(nextByte);
            }
            else {
                break;
            }
        } while (true);

        inputStream.close();
        outputStream.close();
    }
}
