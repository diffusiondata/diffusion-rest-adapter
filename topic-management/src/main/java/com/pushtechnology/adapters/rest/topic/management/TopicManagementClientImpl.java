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

import java.util.HashMap;
import java.util.Map;

import com.pushtechnology.adapters.rest.endpoints.EndpointType;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.callbacks.Registration;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.AddCallback;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.Bytes;

import net.jcip.annotations.GuardedBy;

/**
 * Topic management client to control Diffusion topic tree.
 *
 * @author Push Technology Limited
 */
public final class TopicManagementClientImpl implements TopicManagementClient {
    @GuardedBy("this")
    private final Map<ServiceConfig, Registration> handles = new HashMap<>();
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
            .removeTopicsWithSession(serviceConfig.getTopicPathRoot())
            .thenAccept(handle -> {
                synchronized (this) {
                    handles.put(serviceConfig, handle);
                }
            });
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addEndpoint(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            AddCallback callback) {

        final String produces = endpointConfig.getProduces();
        final String topicPath = serviceConfig.getTopicPathRoot() + "/" + endpointConfig.getTopicPath();
        final TopicType topicType = EndpointType.from(produces).getTopicType();

        session
            .feature(TopicControl.class)
            .addTopic(topicPath, topicType, new TopicSetupCallback(callback));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addEndpoint(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            Bytes initialValue,
            AddCallback callback) {

        final String produces = endpointConfig.getProduces();
        final String topicPath = serviceConfig.getTopicPathRoot() + "/" + endpointConfig.getTopicPath();
        final TopicType topicType = EndpointType.from(produces).getTopicType();

        session
            .feature(TopicControl.class)
            .addTopic(topicPath, topicType, initialValue, new TopicSetupCallback(callback));
    }

    @Override
    public void removeEndpoint(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        session
            .feature(TopicControl.class)
            .removeTopics(serviceConfig.getTopicPathRoot() + "/" + endpointConfig.getTopicPath());
    }

    @Override
    public void removeService(ServiceConfig serviceConfig) {
        synchronized (this) {
            final Registration handle = handles.remove(serviceConfig);
            if (handle != null) {
                handle.close();
            }
        }
    }
}
