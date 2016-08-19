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

package com.pushtechnology.adapters.rest.model.store;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import com.pushtechnology.adapters.rest.model.latest.Model;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * An mutable model store that notifies listeners asynchronously.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class AsyncMutableModelStore implements ModelStore {
    private final Collection<Consumer<Model>> listeners = new CopyOnWriteArrayList<>();
    private final Object modelMutex = new Object();
    private final Object notificationMutex = new Object();
    private final Executor executor;
    @GuardedBy("modelMutex")
    private Model model;
    @GuardedBy("notificationMutex")
    private int notificationVersion;

    /**
     * Constructor.
     * @param executor the executor to notify on
     */
    public AsyncMutableModelStore(Executor executor) {
        this.executor = executor;
    }

    @Override
    public synchronized Model get() {
        synchronized (modelMutex) {
            return model;
        }
    }

    /**
     * Atomically update the model used by the store.
     * @param operation the operation to apply to the current model
     * @return the store
     */
    public AsyncMutableModelStore apply(Function<Model, Model> operation) {
        synchronized (modelMutex) {
            final Model newModel = operation.apply(model);

            if (newModel.equals(model)) {
                return this;
            }

            model = newModel;

            synchronized (notificationMutex) {
                notificationVersion += 1;
                notifyListeners(notificationVersion, newModel);
            }
        }
        return this;
    }

    /**
     * Set the model used by the store.
     * @param newModel the new model
     * @return the store
     */
    public AsyncMutableModelStore setModel(Model newModel) {
        return apply(model -> newModel);
    }

    @Override
    public void onModelChange(Consumer<Model> listener) {
        listeners.add(listener);

        synchronized (modelMutex) {
            if (model == null) {
                return;
            }

            synchronized (notificationMutex) {
                listener.accept(model);
            }
        }
    }

    /**
     * Notify the listeners of a model change.
     */
    private void notifyListeners(int newNotificationVersion, Model newModel) {
        executor.execute(() -> {
            synchronized (notificationMutex) {
                if (notificationVersion != newNotificationVersion) {
                    return;
                }

                listeners.forEach(listeners1 -> listeners1.accept(newModel));
            }
        });
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
