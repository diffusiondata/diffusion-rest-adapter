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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.callbacks.Registration;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Implemented {@link EventedUpdateSource}.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class EventedUpdateSourceImpl implements EventedUpdateSource {
    private final Object mutex = new Object();
    private final InvertedUpdateSource updateSource = new InvertedUpdateSource();
    private final String registeredTopicPath;

    @GuardedBy("mutex")
    private final List<Consumer<Updater>> onActiveEventHandlers = new ArrayList<>();
    @GuardedBy("mutex")
    private final List<Consumer<ErrorReason>> onErrorEventHandlers = new ArrayList<>();
    @GuardedBy("mutex")
    private final List<Runnable> onStandbyEventHandlers = new ArrayList<>();
    @GuardedBy("mutex")
    private final List<Runnable> onCloseEventHandlers = new ArrayList<>();

    @GuardedBy("mutex")
    private State state = State.UNREGISTERED;
    @GuardedBy("mutex")
    private Registration registration;
    @GuardedBy("mutex")
    private Updater currentUpdater;
    @GuardedBy("mutex")
    private ErrorReason currentErrorReason;

    /**
     * Constructor.
     */
    public EventedUpdateSourceImpl(String registeredTopicPath) {
        this.registeredTopicPath = registeredTopicPath;
    }

    @Override
    public EventedUpdateSource onActive(Consumer<Updater> eventHandler) {
        synchronized (mutex) {
            onActiveEventHandlers.add(eventHandler);
            if (state == State.ACTIVE) {
                eventHandler.accept(currentUpdater);
            }
        }
        return this;
    }

    @Override
    public EventedUpdateSource onStandby(Runnable eventHandler) {
        synchronized (mutex) {
            onStandbyEventHandlers.add(eventHandler);
            if (state == State.STANDBY) {
                eventHandler.run();
            }
        }
        return this;
    }

    @Override
    public EventedUpdateSource onClose(Runnable eventHandler) {
        synchronized (mutex) {
            onCloseEventHandlers.add(eventHandler);
            if (state == State.CLOSED) {
                eventHandler.run();
            }
        }
        return this;
    }

    @Override
    public EventedUpdateSource onError(Consumer<ErrorReason> eventHandler) {
        synchronized (mutex) {
            onErrorEventHandlers.add(eventHandler);
            if (state == State.ERRORED) {
                eventHandler.accept(currentErrorReason);
            }
        }
        return this;
    }

    @Override
    public EventedUpdateSource register(TopicUpdateControl updateControl) {
        synchronized (mutex) {
            if (state != State.UNREGISTERED) {
                throw new IllegalStateException("An EventedUpdateSource cannot be registered twice");
            }
            state = State.STANDBY;
            onStandbyEventHandlers.forEach(Runnable::run);

            updateControl.registerUpdateSource(registeredTopicPath, updateSource);
        }
        return this;
    }

    @Override
    public void close() {
        synchronized (mutex) {
            if (registration != null) {
                registration.close();
            }
            state = State.CLOSING;
        }
    }

    private final class InvertedUpdateSource implements TopicUpdateControl.UpdateSource {
        @Override
        public void onActive(String topicPath, Updater updater) {
            synchronized (mutex) {
                if (state == State.STANDBY) {
                    state = State.ACTIVE;
                    currentUpdater = updater;
                    onActiveEventHandlers.forEach(handler -> handler.accept(updater));
                }
            }
        }

        @Override
        public void onStandby(String topicPath) {
            synchronized (mutex) {
                if (state == State.ACTIVE) {
                    state = State.STANDBY;
                    onStandbyEventHandlers.forEach(Runnable::run);
                }
            }
        }

        @Override
        public void onRegistered(String topicPath, Registration newRegistration) {
            synchronized (mutex) {
                if (state == State.CLOSING) {
                    newRegistration.close();
                }
                else {
                    registration = newRegistration;
                }
            }
        }

        @Override
        public void onClose(String topicPath) {
            synchronized (mutex) {
                state = State.CLOSED;
                onCloseEventHandlers.forEach(Runnable::run);
            }
        }

        @Override
        public void onError(String topicPath, ErrorReason errorReason) {
            synchronized (mutex) {
                if (errorReason == ErrorReason.SESSION_CLOSED) {
                    state = State.CLOSED;
                    onCloseEventHandlers.forEach(Runnable::run);
                }
                else {
                    state = State.ERRORED;
                    currentErrorReason = errorReason;
                    onErrorEventHandlers.forEach(handler -> handler.accept(errorReason));
                }
            }
        }
    }

    private enum State {
        UNREGISTERED,
        CLOSING,
        CLOSED,
        ACTIVE,
        STANDBY,
        ERRORED
    }
}
