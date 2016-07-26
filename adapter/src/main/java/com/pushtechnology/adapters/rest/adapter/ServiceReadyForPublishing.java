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

import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.EXISTS;

import java.util.function.Consumer;

import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.AddCallback;

/**
 * Handler for a service that is now ready for publishing to.
 *
 * @author Push Technology Limited
 */
/*package*/ final class ServiceReadyForPublishing implements Consumer<ServiceConfig> {
    private final TopicManagementClient topicManagementClient;
    private final ServiceSession serviceSession;

    /*package*/ ServiceReadyForPublishing(TopicManagementClient topicManagementClient, ServiceSession serviceSession) {
        this.topicManagementClient = topicManagementClient;
        this.serviceSession = serviceSession;
    }

    @Override
    public void accept(ServiceConfig serviceConfig) {
        serviceConfig
            .getEndpoints()
            .forEach(endpoint -> topicManagementClient.addEndpoint(serviceConfig, endpoint, new AddCallback() {
                @Override
                public void onTopicAdded(String topicPath) {
                    serviceSession.addEndpoint(endpoint);
                }

                @Override
                public void onTopicAddFailed(String topicPath, TopicAddFailReason reason) {
                    if (reason == EXISTS) {
                        onTopicAdded(topicPath);
                    }
                }

                @Override
                public void onDiscard() {
                }
            }));
        serviceSession.start();
    }
}
