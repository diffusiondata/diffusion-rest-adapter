/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.diffusion.client.session.Session;

/**
 * Internal listener for the state of the adapter listener.
 *
 * @author Matt Champion 16/09/2017
 */
/*package*/ interface RESTAdapterListener {

    /**
     * Notified when the Diffusion session is opened.
     */
    void onSessionOpen(Session session);

    /**
     * Notified when the adapter is reconfigured.
     */
    void onReconfiguration(Model model);

    /**
     * Notified when the Diffusion session is lost.
     */
    void onSessionLost(Session session);

    /**
     * Notified when the Diffusion session is closed.
     */
    void onSessionClosed(Session session);
}
