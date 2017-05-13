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

package com.pushtechnology.adapters.rest.topic.management;

import com.pushtechnology.adapters.rest.metrics.TopicCreationListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Implementation of {@link ListenerNotifier} for topics with an initial value.
 *
 * @author Matt Champion 13/05/2017
 */
/*package*/ final class ListenerNotifierWithInitialValue implements ListenerNotifier {
    private final TopicCreationListener topicCreationListener;
    private final ServiceConfig serviceConfig;
    private final EndpointConfig endpointConfig;
    private final Bytes initialValue;

    /**
     * Constructor.
     */
    /*package*/ ListenerNotifierWithInitialValue(
            TopicCreationListener topicCreationListener,
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            Bytes initialValue) {
        this.topicCreationListener = topicCreationListener;
        this.serviceConfig = serviceConfig;
        this.endpointConfig = endpointConfig;
        this.initialValue = initialValue;
    }

    @Override
    public void notifyTopicCreated() {
        topicCreationListener.onTopicCreated(serviceConfig, endpointConfig, initialValue);
    }

    @Override
    public void notifyTopicCreationFailed(TopicAddFailReason reason) {
        topicCreationListener.onTopicCreationFailed(serviceConfig, endpointConfig, initialValue, reason);
    }
}
