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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener.PublicationCompletionListener;
import com.pushtechnology.diffusion.client.content.update.Update;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.BinaryDelta;
import com.pushtechnology.diffusion.datatype.Bytes;
import com.pushtechnology.diffusion.datatype.DataType;
import com.pushtechnology.diffusion.datatype.DeltaType;

/**
 * Implementation of {@link UpdateContext} that supports recovery.
 *
 * @param <T> The type of updates the context accepts
 * @author Push Technology Limited
 */
@SuppressWarnings("deprecation")
/*package*/ final class UpdateContextImpl<T> implements UpdateContext<T>, Session.Listener {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateContextImpl.class);
    private final AtomicReference<CachedRequest<T>> cachedValue = new AtomicReference<>(null);
    private final DataType<T> dataType;
    private final ListenerNotifier listenerNotifier;
    private final Updater updater;
    private final Session session;
    private final String topicPath;
    private final Function<BinaryDelta, Bytes> deltaToBytes;
    private final Function<Bytes, Update> bytesToDeltaUpdate;
    private final DeltaType<T, BinaryDelta> deltaType;
    private T lastPublishedValue;

    /**
     * Constructor.
     */
    UpdateContextImpl(
            Session session,
            Updater updater,
            String topicPath,
            DataType<T> dataType,
            Function<Bytes, Update> bytesToDeltaUpdate,
            ListenerNotifier listenerNotifier) {
        this.session = session;
        this.updater = updater;
        this.topicPath = topicPath;
        this.dataType = dataType;
        this.listenerNotifier = listenerNotifier;

        deltaType = dataType.deltaType(BinaryDelta.class);
        deltaToBytes = deltaType::toBytes;
        this.bytesToDeltaUpdate = bytesToDeltaUpdate;
    }

    @Override
    public void onSessionStateChanged(Session changedSession, Session.State oldState, Session.State newState) {
        if (changedSession == session && oldState.isRecovering() && newState.isConnected()) {
            final CachedRequest<T> cachedRequest = cachedValue.getAndSet(null);
            if (cachedRequest != null) {
                LOG.debug("Publishing cached value on recovery");
                lastPublishedValue = cachedRequest.value;
                updater.update(
                    topicPath,
                    dataType.toBytes(cachedRequest.value),
                    topicPath,
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

        if (state.isRecovering()) {
            LOG.debug("Caching value while in recovery");
            final Bytes bytes = dataType.toBytes(value);
            final PublicationCompletionListener completionListener = listenerNotifier.notifyPublicationRequest(bytes);
            cachedValue.set(new CachedRequest<>(value, completionListener));
            return;
        }

        if (lastPublishedValue != null) {
            final BinaryDelta delta = deltaType.diff(lastPublishedValue, value);
            final Bytes bytes = deltaToBytes.apply(delta);
            final PublicationCompletionListener completionListener = listenerNotifier.notifyPublicationRequest(bytes);
            lastPublishedValue = value;
            updater.update(
                topicPath,
                bytesToDeltaUpdate.apply(bytes),
                topicPath,
                new UpdateTopicCallback(completionListener));
        }
        else {
            final Bytes bytes = dataType.toBytes(value);
            final PublicationCompletionListener completionListener = listenerNotifier.notifyPublicationRequest(bytes);
            lastPublishedValue = value;
            updater.update(
                topicPath,
                bytes,
                topicPath,
                new UpdateTopicCallback(completionListener));
        }
    }

    private static final class CachedRequest<T> {
        private final T value;
        private final PublicationCompletionListener completionListener;

        private CachedRequest(T value, PublicationCompletionListener completionListener) {
            this.value = value;
            this.completionListener = completionListener;
        }
    }
}
