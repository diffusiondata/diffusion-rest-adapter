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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.pushtechnology.adapters.rest.metrics.listeners.PollListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;

/**
 * Unit tests for {@link MetricsProvider}.
 *
 * @author Push Technology Limited
 */
public final class MetricsProviderTest {
    @Mock
    private Runnable startTask;
    @Mock
    private Runnable stopTask;
    @Mock
    private PollListener pollListener;
    @Mock
    private PublicationListener publicationListener;
    @Mock
    private TopicCreationListener topicCreationListener;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @After
    public void postConditions() {
        verifyNoMoreInteractions(startTask, stopTask, pollListener, publicationListener, topicCreationListener);
    }

    @Test
    public void start() throws Exception {
        final MetricsProvider metricsProvider = new MetricsProvider(
            startTask,
            stopTask,
            pollListener,
            publicationListener,
            topicCreationListener);

        metricsProvider.start();

        verify(startTask).run();
    }

    @Test
    public void close() throws Exception {
        final MetricsProvider metricsProvider = new MetricsProvider(
            startTask,
            stopTask,
            pollListener,
            publicationListener,
            topicCreationListener);

        metricsProvider.close();

        verify(stopTask).run();
    }

    @Test
    public void onPollRequest() throws Exception {
        final MetricsProvider metricsProvider = new MetricsProvider(
            startTask,
            stopTask,
            pollListener,
            publicationListener,
            topicCreationListener);

        metricsProvider.onPollRequest(null, null);

        verify(pollListener).onPollRequest(null, null);
    }

    @Test
    public void onTopicCreationRequest() throws Exception {
        final MetricsProvider metricsProvider = new MetricsProvider(
            startTask,
            stopTask,
            pollListener,
            publicationListener,
            topicCreationListener);

        metricsProvider.onTopicCreationRequest(null, null);

        verify(topicCreationListener).onTopicCreationRequest(null, null);
    }

    @Test
    public void onTopicCreationRequestWithInitialValue() throws Exception {
        final MetricsProvider metricsProvider = new MetricsProvider(
            startTask,
            stopTask,
            pollListener,
            publicationListener,
            topicCreationListener);

        metricsProvider.onTopicCreationRequest(null, null, null);

        verify(topicCreationListener).onTopicCreationRequest(null, null, null);
    }

    @Test
    public void onPublicationRequest() throws Exception {
        final MetricsProvider metricsProvider = new MetricsProvider(
            startTask,
            stopTask,
            pollListener,
            publicationListener,
            topicCreationListener);

        metricsProvider.onPublicationRequest(null, null, null);

        verify(publicationListener).onPublicationRequest(null, null, null);
    }

}