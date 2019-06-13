/*******************************************************************************
 * Copyright (C) 2019 Push Technology Ltd.
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

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.ValueUpdater;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.Bytes;
import com.pushtechnology.diffusion.datatype.DataType;

/**
 * Update context that publishes values.
 *
 * @param <T> The type of updates the context accepts
 * @author Push Technology Limited
 */
/*package*/ final class ValueUpdateContext<T> implements UpdateContext<T>, Session.Listener {
    private static final Logger LOG = LoggerFactory.getLogger(ValueUpdateContext.class);
    private final AtomicReference<ValueUpdateContext.CachedRequest<T>> cachedValue = new AtomicReference<>(null);
    private final DataType<T> dataType;
    private final PublicationListener listener;
    private final ValueUpdater<T> updater;
    private final Session session;
    private final String path;

    /**
     * Constructor.
     */
    ValueUpdateContext(
            Session session,
            TopicUpdateControl.Updater updater,
            String path,
            Class<T> valueClass,
            DataType<T> dataType,
            PublicationListener listener) {
        this.dataType = dataType;
        this.listener = listener;
        this.updater = updater.valueUpdater(valueClass);
        this.session = session;
        this.path = path;
    }

    @Override
    public void onSessionStateChanged(Session changedSession, Session.State oldState, Session.State newState) {
        if (changedSession == session && oldState.isRecovering() && newState.isConnected()) {
            final ValueUpdateContext.CachedRequest<T> cachedRequest = cachedValue.getAndSet(null);
            if (cachedRequest != null) {
                LOG.debug("Publishing cached value on recovery");
                updater.update(
                    path,
                    cachedRequest.value,
                    path,
                    new UpdateTopicCallback(cachedRequest.completionListener));
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
            final Bytes bytes = dataType.toBytes(value);
            final PublicationListener.PublicationCompletionListener completionListener =
                listener.onPublicationRequest(path, bytes.length());
            cachedValue.set(new ValueUpdateContext.CachedRequest<>(value, completionListener));
        }
        else {
            applyValue(value);
        }
    }

    private void applyValue(T value) {
        final Bytes bytes = dataType.toBytes(value);
        final PublicationListener.PublicationCompletionListener completionListener =
            listener.onPublicationRequest(path, bytes.length());
        updater.update(
            path,
            value,
            path,
            new UpdateTopicCallback(completionListener));
    }

    private static final class CachedRequest<T> {
        private final T value;
        private final PublicationListener.PublicationCompletionListener completionListener;

        private CachedRequest(
            T value,
            PublicationListener.PublicationCompletionListener completionListener) {

            this.value = value;
            this.completionListener = completionListener;
        }
    }
}
