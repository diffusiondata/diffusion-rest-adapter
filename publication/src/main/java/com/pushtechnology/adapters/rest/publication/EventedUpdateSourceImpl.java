/*******************************************************************************
 * Copyright (C) 2020 Push Technology Ltd.
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
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.Session.SessionLock;
import com.pushtechnology.diffusion.client.session.SessionClosedException;

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
    private final String serviceScope;

    @GuardedBy("mutex")
    private final List<Consumer<SessionLock>> onActiveEventHandlers = new ArrayList<>();
    @GuardedBy("mutex")
    private final List<Consumer<ErrorReason>> onErrorEventHandlers = new ArrayList<>();
    @GuardedBy("mutex")
    private final List<Runnable> onStandbyEventHandlers = new ArrayList<>();
    @GuardedBy("mutex")
    private final List<Runnable> onCloseEventHandlers = new ArrayList<>();

    @GuardedBy("mutex")
    private State state = State.UNREGISTERED;
    @GuardedBy("mutex")
    private CompletableFuture<?> registration;
    @GuardedBy("mutex")
    private SessionLock currentLock;
    @GuardedBy("mutex")
    private ErrorReason currentErrorReason;

    /**
     * Constructor.
     */
    public EventedUpdateSourceImpl(String serviceScope) {
        this.serviceScope = serviceScope;
    }

    @Override
    public EventedUpdateSource onActive(Consumer<SessionLock> eventHandler) {
        synchronized (mutex) {
            onActiveEventHandlers.add(eventHandler);
            if (state == State.ACTIVE) {
                eventHandler.accept(currentLock);
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
    public EventedUpdateSource register(Session session) {
        synchronized (mutex) {
            if (state != State.UNREGISTERED) {
                throw new IllegalStateException("An EventedUpdateSource cannot be registered twice");
            }

            state = State.STANDBY;
            onStandbyEventHandlers.forEach(Runnable::run);

            registration = session
                .lock(serviceScope, Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS)
                .whenComplete((lock, exception) -> {
                    synchronized (mutex) {
                        if (exception == null) {
                            if (state == State.STANDBY) {
                                state = State.ACTIVE;
                                currentLock = lock;
                                onActiveEventHandlers.forEach(handler -> handler.accept(lock));
                            }
                            else if (state == State.CLOSING) {
                                state = State.CLOSED;
                                onCloseEventHandlers.forEach(Runnable::run);
                            }
                        }
                        else if (exception instanceof CompletionException) {
                            final Throwable cause = exception.getCause();
                            if (cause instanceof SessionClosedException) {
                                state = State.CLOSED;
                                onCloseEventHandlers.forEach(Runnable::run);
                            }
                            else if (cause instanceof CancellationException) {
                                state = State.CLOSED;
                                onCloseEventHandlers.forEach(Runnable::run);
                            }
                            else {
                                state = State.ERRORED;
                                currentErrorReason = ErrorReason.COMMUNICATION_FAILURE;
                                onErrorEventHandlers.forEach(handler -> handler.accept(currentErrorReason));
                            }
                        }
                        else {
                            state = State.ERRORED;
                            currentErrorReason = ErrorReason.COMMUNICATION_FAILURE;
                            onErrorEventHandlers.forEach(handler -> handler.accept(currentErrorReason));
                        }
                    }
                });
        }
        return this;
    }

    @Override
    public void close() {
        synchronized (mutex) {
            if (currentLock != null) {
                currentLock.unlock();
                state = State.CLOSED;
                onCloseEventHandlers.forEach(Runnable::run);
            }
            else if (registration != null) {
                registration.cancel(false);
                state = State.CLOSING;
            }
            else {
                state = State.CLOSED;
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
