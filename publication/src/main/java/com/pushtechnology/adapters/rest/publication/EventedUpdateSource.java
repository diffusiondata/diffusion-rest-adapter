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

import java.util.function.Consumer;

import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.Session.SessionLock;

/**
 * Update source that emits events.
 * <P>
 * An update source is created first and then registered. This allows event handlers to be registered before it is
 * registered.
 *
 * @author Push Technology Limited
 */
public interface EventedUpdateSource extends AutoCloseable {
    /**
     * Add an event handler for active events.
     */
    EventedUpdateSource onActive(Consumer<SessionLock> eventHandler);

    /**
     * Add an event handler for standby events.
     */
    EventedUpdateSource onStandby(Runnable eventHandler);

    /**
     * Add an event handler for close events.
     */
    EventedUpdateSource onClose(Runnable eventHandler);

    /**
     * Add an event handler for error events.
     */
    EventedUpdateSource onError(Consumer<ErrorReason> eventHandler);

    /**
     * Register the update source. An update source can only be registered once.
     */
    EventedUpdateSource register(Session session);

    /**
     * Close the update source.
     */
    @Override
    void close();
}
