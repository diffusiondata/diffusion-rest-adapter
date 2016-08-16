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

import com.pushtechnology.adapters.rest.model.latest.Model;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * A {@link ModelStore} that can be modified.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class MutableModelStore extends AbstractModelStore {
    @GuardedBy("this")
    private Model model;

    @Override
    public synchronized Model get() {
        return model;
    }

    /**
     * Update the model used by the store.
     * @param newModel the new model
     * @return the store
     */
    public synchronized MutableModelStore setModel(Model newModel) {
        if (newModel.equals(model)) {
            return this;
        }

        model = newModel;
        notifyListeners(newModel);
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
