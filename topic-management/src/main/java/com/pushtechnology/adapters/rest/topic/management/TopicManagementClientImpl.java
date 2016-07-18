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

package com.pushtechnology.adapters.rest.topic.management;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.callbacks.TopicTreeHandler;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicType;

/**
 * Topic management client to control Diffusion topic tree.
 *
 * @author Push Technology Limited
 */
public final class TopicManagementClientImpl implements TopicManagementClient {
    private final Session session;

    /**
     * Constructor.
     */
    public TopicManagementClientImpl(Session session) {
        this.session = session;
    }

    @Override
    public void addService(ServiceConfig serviceConfig) {
        session
            .feature(TopicControl.class)
            .removeTopicsWithSession(serviceConfig.getTopicRoot(), new TopicTreeHandler.Default());
    }

    @Override
    public void addEndpoint(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            TopicControl.AddCallback callback) {
        session
            .feature(TopicControl.class)
            .addTopic(
                serviceConfig.getTopicRoot() + "/" + endpointConfig.getTopic(),
                TopicType.JSON,
                callback);
    }
}
