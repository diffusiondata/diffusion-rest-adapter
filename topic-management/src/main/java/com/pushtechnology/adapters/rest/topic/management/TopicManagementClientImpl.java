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

import java.nio.charset.Charset;

import com.pushtechnology.adapters.rest.model.EndpointType;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.callbacks.TopicTreeHandler;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.AddCallback;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.Bytes;
import com.pushtechnology.diffusion.datatype.binary.BinaryDataType;

/**
 * Topic management client to control Diffusion topic tree.
 *
 * @author Push Technology Limited
 */
public final class TopicManagementClientImpl implements TopicManagementClient {
    private static final BinaryDataType BINARY_DATA_TYPE = Diffusion.dataTypes().binary();
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
            AddCallback callback) {

        final String produces = endpointConfig.getProduces();
        final String topicPath = serviceConfig.getTopicRoot() + "/" + endpointConfig.getTopic();
        final TopicType topicType = EndpointType.from(produces).getTopicType();

        // Addition synchronisation to partially work around FB: 14967
        synchronized (this) {
            session
                .feature(TopicControl.class)
                .addTopic(topicPath, topicType, new TopicSetupCallback(callback));
        }
    }

    @Override
    public <V> void addEndpoint(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            V initialValue,
            AddCallback callback) {

        final Bytes value;
        if (initialValue instanceof String) {
            final String stringValue = (String) initialValue;
            value = BINARY_DATA_TYPE.readValue(stringValue.getBytes(Charset.forName("UTF-8")));
        }
        else if (initialValue instanceof Bytes) {
            value = (Bytes) initialValue;
        }
        else {
            throw new IllegalArgumentException("Values of the type " + initialValue.getClass() + " are not supported");
        }

        final String produces = endpointConfig.getProduces();
        final String topicPath = serviceConfig.getTopicRoot() + "/" + endpointConfig.getTopic();
        final TopicType topicType = EndpointType.from(produces).getTopicType();

        // Addition synchronisation to partially work around FB: 14967
        synchronized (this) {
            session
                .feature(TopicControl.class)
                .addTopic(topicPath, topicType, value, new TopicSetupCallback(callback));
        }
    }
}
