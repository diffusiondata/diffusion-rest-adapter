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
import java.util.concurrent.atomic.AtomicBoolean;

import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.polling.PollClient;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.diffusion.client.session.Session;

/**
 * The {@link com.pushtechnology.adapters.rest.component.Component} responsible for publishing to a Diffusion server.
 *
 * @author Push Technology Limited
 */
/*package*/ final class PublicationComponentImpl implements PublicationComponent {
    private final AtomicBoolean isActive;
    private final Session session;
    private final TopicManagementClient topicManagementClient;
    private final PublishingClient publishingClient;
    private final PollingComponentFactory pollingComponentFactory;

    /**
     * Constructor.
     */
    /*package*/ PublicationComponentImpl(
            AtomicBoolean isActive,
            Session session,
            TopicManagementClient topicManagementClient,
            PublishingClient publishingClient,
            PollingComponentFactory pollingComponentFactory) {
        this.isActive = isActive;
        this.session = session;
        this.topicManagementClient = topicManagementClient;
        this.publishingClient = publishingClient;
        this.pollingComponentFactory = pollingComponentFactory;
    }

    @Override
    public void close() throws IOException {
        isActive.set(false);
        session.close();
    }

    @Override
    public PollingComponent createPolling(Model model, PollClient pollClient) {
        return pollingComponentFactory.create(model, pollClient, publishingClient, topicManagementClient);
    }
}
