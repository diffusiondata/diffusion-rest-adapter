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

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;

import com.pushtechnology.diffusion.client.session.Session;

/**
 * Listener for loss of the session. Invokes the task passed into it when the session closes before the listener.
 *
 * @author Push Technology Limited
 */
public final class SessionLostListener implements Session.Listener, AutoCloseable {
    private final AtomicBoolean isActive = new AtomicBoolean(true);

    private final Runnable shutdownTask;

    /**
     * Constructor.
     */
    public SessionLostListener(Runnable shutdownTask) {
        this.shutdownTask = shutdownTask;
    }

    @Override
    public void onSessionStateChanged(Session session, Session.State oldState, Session.State newState) {
        if (isActive.get() && newState.isClosed()) {
            shutdownTask.run();
        }
    }

    @PreDestroy
    @Override
    public void close() {
        isActive.set(false);
    }
}
