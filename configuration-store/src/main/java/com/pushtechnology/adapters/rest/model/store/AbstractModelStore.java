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

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import com.pushtechnology.adapters.rest.model.latest.Model;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * An abstract {@link ModelStore} implementation that manages the listeners.
 * <P>
 * Synchronises on the object instances when accessing the listeners.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
/*package*/ abstract class AbstractModelStore implements ModelStore {
    @GuardedBy("this")
    private final Collection<Consumer<Model>> listeners = new ArrayList<>();

    @Override
    public final synchronized void onModelChange(Consumer<Model> listener) {
        listeners.add(listener);
        final Model model = get();
        if (model != null) {
            listener.accept(model);
        }
    }

    /**
     * Notify the listeners of a model change.
     * @param newModel The new model
     */
    protected final synchronized void notifyListeners(Model newModel) {
        listeners.forEach(listeners -> listeners.accept(newModel));
    }
}
