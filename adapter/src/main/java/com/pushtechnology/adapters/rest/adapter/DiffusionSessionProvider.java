/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

import javax.net.ssl.SSLContext;

import org.picocontainer.annotations.Nullable;
import org.picocontainer.injectors.ProviderAdapter;

import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.session.management.DiffusionSessionFactory;
import com.pushtechnology.adapters.rest.session.management.EventedSessionListener;
import com.pushtechnology.adapters.rest.session.management.SessionLostListener;
import com.pushtechnology.diffusion.client.session.Session;

/**
 * Provider for a Diffusion {@link Session}.
 *
 * @author Matt Champion 06/08/2017
 */
public final class DiffusionSessionProvider extends ProviderAdapter {
    /**
     * Constructor.
     */
    public DiffusionSessionProvider() {
    }

    /**
     * @return an open session
     */
    public Session provide(
        DiffusionSessionFactory factory,
        Model model,
        SessionLostListener sessionLostListener,
        EventedSessionListener listener,
        @Nullable SSLContext sslContext) {

        return factory.openSession(model.getDiffusion(), sessionLostListener, listener, sslContext);
    }
}
