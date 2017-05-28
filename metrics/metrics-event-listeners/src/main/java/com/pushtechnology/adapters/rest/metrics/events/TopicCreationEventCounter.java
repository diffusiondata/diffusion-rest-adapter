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

package com.pushtechnology.adapters.rest.metrics.events;

import com.pushtechnology.adapters.rest.metrics.TopicCreationListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Implementation of {@link EventCounter} for topic creation events.
 *
 * @author Matt Champion 28/05/2017
 */
public final class TopicCreationEventCounter extends AbstractEventCounter implements TopicCreationListener {
    private final TopicCreationCompletionListener completionListener = new TopicCreationCompletionListener() {
        @Override
        public void onTopicCreated() {
            onSuccess();
        }

        @Override
        public void onTopicCreationFailed(TopicAddFailReason reason) {
            onFailure();
        }
    };

    @Override
    public TopicCreationCompletionListener onTopicCreationRequest(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig) {
        onRequest();
        return completionListener;
    }

    @Override
    public TopicCreationCompletionListener onTopicCreationRequest(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            Bytes value) {
        onRequest();
        return completionListener;
    }
}
