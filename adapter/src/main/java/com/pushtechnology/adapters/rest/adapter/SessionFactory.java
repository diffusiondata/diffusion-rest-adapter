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

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;

import javax.net.ssl.SSLContext;

import org.picocontainer.annotations.Nullable;
import org.picocontainer.injectors.ProviderAdapter;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.session.Session;

/**
 * Factory for session.
 *
 * @author Push Technology Limited
 */
public final class SessionFactory extends ProviderAdapter {
    /**
     * @return an open session
     */
    public Session provide(Model model, SessionListener listener, @Nullable SSLContext sslContext) {
        final DiffusionConfig diffusionConfig = model.getDiffusion();

        com.pushtechnology.diffusion.client.session.SessionFactory sessionFactory = Diffusion
            .sessions()
            .serverHost(diffusionConfig.getHost())
            .serverPort(diffusionConfig.getPort())
            .secureTransport(false)
            .transports(WEBSOCKET)
            .reconnectionTimeout(5000)
            .listener(listener);

        if (diffusionConfig.isSecure()) {
            sessionFactory = sessionFactory.secureTransport(true);
        }

        if (sslContext != null) {
            sessionFactory = sessionFactory.sslContext(sslContext);
        }

        if (diffusionConfig.getPrincipal() != null && diffusionConfig.getPassword() != null) {
            sessionFactory = sessionFactory
                .principal(diffusionConfig.getPrincipal())
                .password(diffusionConfig.getPassword());
        }

        return sessionFactory.open();
    }
}
