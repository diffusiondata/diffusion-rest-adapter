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
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener.TopicCreationCompletionListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.callbacks.Registration;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.AddCallback;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicType;

import net.jcip.annotations.GuardedBy;

/**
 * Topic management client to control Diffusion topic tree.
 *
 * @author Push Technology Limited
 */
public final class TopicManagementClientImpl implements TopicManagementClient {
    @GuardedBy("this")
    private final Map<ServiceConfig, Registration> handles = new HashMap<>();
    private final TopicCreationListener topicCreationListener;
    private final Session session;

    /**
     * Constructor.
     */
    public TopicManagementClientImpl(TopicCreationListener topicCreationListener, Session session) {
        this.topicCreationListener = topicCreationListener;
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

    @Override
    public void addEndpoint(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            AddCallback callback) {

        final String produces = endpointConfig.getProduces();
        final String topicPath = serviceConfig.getTopicPathRoot() + "/" + endpointConfig.getTopicPath();
        final TopicType topicType = EndpointType.from(produces).getTopicType();

        final TopicCreationCompletionListener completionListener =
            topicCreationListener.onTopicCreationRequest(topicPath, topicType);
        session
            .feature(TopicControl.class)
            .addTopic(
                topicPath,
                topicType)
                .thenAccept(x -> {
                    completionListener.onTopicCreated();
                    callback.onTopicAdded(topicPath);
                })
                .exceptionally(t -> {
                    if (t instanceof TopicControl.InvalidTopicPathException) {
                        completionListener.onTopicCreationFailed(TopicAddFailReason.INVALID_NAME);
                        callback.onTopicAddFailed(topicPath, TopicAddFailReason.INVALID_NAME);
                    }
                    else if (t instanceof TopicControl.IncompatibleExistingTopicException) {
                        completionListener.onTopicCreationFailed(TopicAddFailReason.EXISTS_INCOMPATIBLE);
                        callback.onTopicAddFailed(topicPath, TopicAddFailReason.EXISTS_INCOMPATIBLE);
                    }
                    else if (t instanceof TopicControl.TopicLicenseLimitException) {
                        completionListener.onTopicCreationFailed(TopicAddFailReason.EXCEEDED_LICENSE_LIMIT);
                        callback.onTopicAddFailed(topicPath, TopicAddFailReason.EXCEEDED_LICENSE_LIMIT);
                    }
                    else if (t instanceof TopicControl.InvalidTopicSpecificationException) {
                        completionListener.onTopicCreationFailed(TopicAddFailReason.INVALID_DETAILS);
                        callback.onTopicAddFailed(topicPath, TopicAddFailReason.INVALID_DETAILS);
                    }
                    else {
                        completionListener.onTopicCreationFailed(TopicAddFailReason.UNEXPECTED_ERROR);
                        callback.onDiscard();
                    }
                    return null;
                });
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
