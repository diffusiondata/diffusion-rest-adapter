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

package com.pushtechnology.adapters.rest.adapter;

import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Listener for service state changes.
 *
 * @author Push Technology Limited
 */
public interface ServiceListener {
    /**
     * Null object listener.
     */
    ServiceListener NULL_LISTENER = new ServiceListener() {
        @Override
        public void onActive(ServiceConfig serviceConfig) {
        }

        @Override
        public void onStandby(ServiceConfig serviceConfig) {
        }

        @Override
        public void onRemove(ServiceConfig serviceConfig) {
        }
    };

    /**
     * Notification that a service is active.
     */
    void onActive(ServiceConfig serviceConfig);

    /**
     * Notification that a service is on standby.
     */
    void onStandby(ServiceConfig serviceConfig);

    /**
     * Notification that a service has been removed.
     */
    void onRemove(ServiceConfig serviceConfig);
}
