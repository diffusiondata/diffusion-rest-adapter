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

package com.pushtechnology.adapters.rest.publication;

import static com.pushtechnology.adapters.rest.publication.UpdateTopicCallback.INSTANCE;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.ValueUpdater;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Implementation of {@link UpdateContext} that supports recovery.
 *
 * @param <T> The type of updates the context accepts
 * @author Push Technology Limited
 */
/*package*/ final class UpdateContextImpl<T extends Bytes> implements UpdateContext<T>, Session.Listener {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateContextImpl.class);
    private final AtomicReference<T> cachedValue = new AtomicReference<>(null);
    private final Session session;
    private final ValueUpdater<T> updater;
    private final String topicPath;

    /**
     * Constructor.
     */
    UpdateContextImpl(Session session, ValueUpdater<T> updater, String topicPath) {
        this.session = session;
        this.updater = updater;
        this.topicPath = topicPath;
    }

    @Override
    public void onSessionStateChanged(Session changedSession, Session.State oldState, Session.State newState) {
        if (changedSession == session && oldState.isRecovering() && newState.isConnected()) {
            final T value = cachedValue.getAndSet(null);
            if (value != null) {
                LOG.debug("Publishing cached value on recovery");
                updater.update(topicPath, value, topicPath, INSTANCE);
            }
        }
    }

    @Override
    public void publish(T value) {
        final Session.State state = session.getState();
        if (state.isClosed()) {
            throw new IllegalStateException("Session closed");
        }
        else if (state.isRecovering()) {
            LOG.debug("Caching value while in recovery");
            cachedValue.set(value);
            return;
        }

        updater.update(topicPath, value, topicPath, INSTANCE);
    }
}
