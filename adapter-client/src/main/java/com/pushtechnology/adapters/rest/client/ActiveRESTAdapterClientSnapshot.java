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

package com.pushtechnology.adapters.rest.client;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import com.pushtechnology.diffusion.client.session.Session;

import net.jcip.annotations.ThreadSafe;

/**
 * The snapshot of the {@link RESTAdapterClient} for a configuration model that is actively polling.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
/*package*/ final class ActiveRESTAdapterClientSnapshot implements RESTAdapterClientSnapshot {
    private final ScheduledExecutorService currentExecutor;
    private final Session session;
    private final AtomicBoolean isActive;

    /*package*/ ActiveRESTAdapterClientSnapshot(
        ScheduledExecutorService currentExecutor,
        Session session,
        AtomicBoolean isActive) {

        this.currentExecutor = currentExecutor;
        this.session = session;
        this.isActive = isActive;
    }

    @Override
    public void close() throws IOException {
        isActive.set(false);
        currentExecutor.shutdown();
        session.close();
    }
}
