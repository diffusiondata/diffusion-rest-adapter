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

import java.util.concurrent.atomic.AtomicBoolean;

import com.pushtechnology.diffusion.client.session.Session;

/**
 * The component responsible for publishing to a Diffusion server.
 *
 * @author Push Technology Limited
 */
/*package*/ final class PublicationComponentImpl implements PublicationComponent {
    private final AtomicBoolean isActive;
    private final Session session;

    /**
     * Constructor.
     */
    /*package*/ PublicationComponentImpl(
            AtomicBoolean isActive,
            Session session) {
        this.isActive = isActive;
        this.session = session;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void close() {
        isActive.set(false);
        session.close();
    }
}
