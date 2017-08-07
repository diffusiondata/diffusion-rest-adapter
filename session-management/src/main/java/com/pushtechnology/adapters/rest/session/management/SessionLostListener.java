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

import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_BY_SERVER;
import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_FAILED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.diffusion.client.session.Session;

/**
 * Listener for loss of the session. Invokes the {@link SessionLossHandler} passed into it when the session closes
 * unexpectedly.
 *
 * @author Push Technology Limited
 */
public final class SessionLostListener implements Session.Listener {
    private static final Logger LOG = LoggerFactory.getLogger(SessionLostListener.class);

    private final SessionLossHandler sessionLossHandler;

    /**
     * Constructor.
     */
    public SessionLostListener(SessionLossHandler sessionLossHandler) {
        this.sessionLossHandler = sessionLossHandler;
    }

    @Override
    public void onSessionStateChanged(Session session, Session.State oldState, Session.State newState) {
        if (CLOSED_FAILED.equals(newState) || CLOSED_BY_SERVER.equals(newState)) {
            LOG.warn("Session {} has been lost", session);
            sessionLossHandler.onLoss();
        }
    }
}
