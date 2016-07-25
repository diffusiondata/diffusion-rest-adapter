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

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.diffusion.client.session.Session;

/**
 * The component responsible for publishing to a Diffusion server.
 *
 * @author Push Technology Limited
 */
public final class PublicationComponentImpl implements PublicationComponent {
    private static final Logger LOG = LoggerFactory.getLogger(PollingComponentImpl.class);
    private final AtomicBoolean isActive;
    private final Session session;

    /**
     * Constructor.
     */
    public PublicationComponentImpl(Session session) {
        this.isActive = new AtomicBoolean(true);
        this.session = session;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void start() {
        LOG.info("Opening session component");
        LOG.info("Opened session component");
    }

    @PreDestroy
    @Override
    public void close() {
        LOG.info("Closing session component");
        isActive.set(false);
        session.close();
        LOG.info("Closed session component");
    }
}
