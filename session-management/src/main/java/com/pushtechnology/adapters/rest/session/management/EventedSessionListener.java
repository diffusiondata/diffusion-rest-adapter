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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;

/**
 * A session listener that can be listened to for session state changes. Allows listeners to be attached after the
 * session is opened. Only notifies of changes to the state.
 *
 * @author Push Technology Limited
 */
public final class EventedSessionListener {
    private final InternalListener internal = new InternalListener();
    private final List<Session.Listener> attachedListeners = new CopyOnWriteArrayList<>();

    /**
     * Attach a new listener for state change events.
     */
    public EventedSessionListener onSessionStateChange(Session.Listener listener) {
        attachedListeners.add(listener);
        return this;
    }

    /**
     * Add the listener to a {@link SessionFactory}.
     */
    public SessionFactory addTo(SessionFactory sessionFactory) {
        return sessionFactory.listener(internal);
    }

    private final class InternalListener implements Session.Listener {
        @Override
        public void onSessionStateChanged(Session session, Session.State oldState, Session.State newState) {
            attachedListeners.forEach(listener -> listener.onSessionStateChanged(session, oldState, newState));
        }
    }
}
