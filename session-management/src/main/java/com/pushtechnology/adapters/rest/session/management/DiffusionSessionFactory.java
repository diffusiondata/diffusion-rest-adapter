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

package com.pushtechnology.adapters.rest.session.management;

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;

import javax.net.ssl.SSLContext;

import org.picocontainer.annotations.Nullable;
import org.picocontainer.injectors.ProviderAdapter;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;

/**
 * Factory for {@link Session}s.
 *
 * @author Push Technology Limited
 */
public final class DiffusionSessionFactory extends ProviderAdapter {
    private final SessionFactory baseSessionFactory;

    /**
     * Constructor.
     */
    /*package*/ DiffusionSessionFactory(SessionFactory baseSessionFactory) {
        this.baseSessionFactory = baseSessionFactory.transports(WEBSOCKET);
    }

    /**
     * @return an open session
     */
    public Session provide(Model model, SessionLostListener listener, @Nullable SSLContext sslContext) {
        final DiffusionConfig diffusionConfig = model.getDiffusion();

        SessionFactory sessionFactory = baseSessionFactory
            .serverHost(diffusionConfig.getHost())
            .serverPort(diffusionConfig.getPort())
            .secureTransport(diffusionConfig.isSecure())
            .connectionTimeout(diffusionConfig.getConnectionTimeout())
            .reconnectionTimeout(diffusionConfig.getReconnectionTimeout())
            .maximumMessageSize(diffusionConfig.getMaximumMessageSize())
            .inputBufferSize(diffusionConfig.getInputBufferSize())
            .outputBufferSize(diffusionConfig.getInputBufferSize())
            .recoveryBufferSize(diffusionConfig.getRecoveryBufferSize())
            .listener(listener);

        if (sslContext != null) {
            sessionFactory = sessionFactory.sslContext(sslContext);
        }

        if (diffusionConfig.getPrincipal() != null) {
            sessionFactory = sessionFactory.principal(diffusionConfig.getPrincipal());

            if (diffusionConfig.getPassword() != null) {
                sessionFactory = sessionFactory.password(diffusionConfig.getPassword());
            }
        }

        return sessionFactory.open();
    }

    /**
     * @return a new {@link DiffusionSessionFactory}
     */
    public static DiffusionSessionFactory create() {
        return new DiffusionSessionFactory(Diffusion.sessions());
    }
}
