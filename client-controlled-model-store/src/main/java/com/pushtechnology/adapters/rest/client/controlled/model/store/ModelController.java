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

package com.pushtechnology.adapters.rest.client.controlled.model.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.store.AsyncMutableModelStore;
import com.pushtechnology.diffusion.client.content.Content;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl;
import com.pushtechnology.diffusion.client.session.SessionId;
import com.pushtechnology.diffusion.client.types.ReceiveContext;

import net.jcip.annotations.ThreadSafe;

/**
 * A {@link MessagingControl.MessageHandler} for modifying the model store.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
/*package*/ final class ModelController extends MessagingControl.MessageHandler.Default {
    private static final Logger LOG = LoggerFactory.getLogger(ModelController.class);
    private final AsyncMutableModelStore modelStore;
    private final ModelPublisher modelPublisher;

    /**
     * Constructor.
     */
    /*package*/ ModelController(AsyncMutableModelStore modelStore, ModelPublisher modelPublisher) {
        this.modelStore = modelStore;
        this.modelPublisher = modelPublisher;
    }

    @Override
    public void onMessage(SessionId sessionId, String path, Content content, ReceiveContext receiveContext) {
        if (!ClientControlledModelStore.CONTROL_PATH.equals(path)) {
            LOG.error("Received a message on the wrong path");
            return;
        }

        modelStore.apply(model -> model);
        modelPublisher.update();
    }
}
