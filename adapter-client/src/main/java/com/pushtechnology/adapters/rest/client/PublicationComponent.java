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

import com.pushtechnology.adapters.rest.component.Component;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.polling.HttpComponent;

/**
 * The {@link Component} responsible for polling REST services.
 *
 * @author Push Technology Limited
 */
public interface PublicationComponent extends Component {
    /**
     * Inactive component.
     */
    PublicationComponent INACTIVE = new PublicationComponent() {
        @Override
        public PollingComponent createPolling(Model model, HttpComponent httpComponent) {
            throw new UnsupportedOperationException("A PollingComponent cannot be created from the inactive component");
        }

        @Override
        public void close() throws IOException {
        }
    };

    /**
     * @return A new {@link PollingComponentImpl}
     */
    PollingComponent createPolling(Model model, HttpComponent httpComponent);
}
