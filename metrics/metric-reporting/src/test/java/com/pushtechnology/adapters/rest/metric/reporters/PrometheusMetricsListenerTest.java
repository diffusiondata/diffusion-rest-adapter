/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.metric.reporters;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Enumeration;

import org.junit.Test;

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationSuccessEvent;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.topics.details.TopicType;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

/**
 * Unit tests for {@link PrometheusMetricsListener}.
 *
 * @author Push Technology Limited
 */
public final class PrometheusMetricsListenerTest {
    @Test
    public void onPollRequest() {
        final PrometheusMetricsListener listener = new PrometheusMetricsListener();

        listener.onPollRequest(PollRequestEvent.Factory.create(""));
    }

    @Test
    public void onPollSuccess() {
        final PrometheusMetricsListener listener = new PrometheusMetricsListener();

        assertEquals(getCurrentValue("poll_requests_total"), 0.0, 0.01);
        assertEquals(getCurrentValue("polled_bytes_total"), 0.0, 0.01);

        listener.onPollSuccess(PollSuccessEvent.Factory.create(PollRequestEvent.Factory.create(""), 200, 5));

        assertEquals(getCurrentValue("poll_requests_total"), 1.0, 0.01);
        assertEquals(getCurrentValue("polled_bytes_total"), 5.0, 0.01);
    }

    @Test
    public void onPollFailed() {
        final PrometheusMetricsListener listener = new PrometheusMetricsListener();

        listener.onPollFailed(PollFailedEvent.Factory.create(
            PollRequestEvent.Factory.create(""),
            new Exception("test")));

        assertEquals(getCurrentValue("poll_requests_failed_total", "exceptionMessage", "test"), 1.0, 0.01);
    }

    @Test
    public void onPublicationRequest() {
        final PrometheusMetricsListener listener = new PrometheusMetricsListener();

        listener.onPublicationRequest(PublicationRequestEvent.Factory.create("", 10));
    }

    @Test
    public void onPublicationSuccess() {
        final PrometheusMetricsListener listener = new PrometheusMetricsListener();

        assertEquals(getCurrentValue("updates_published_total"), 0.0, 0.01);
        assertEquals(getCurrentValue("update_bytes_total"), 0.0, 0.01);

        listener.onPublicationSuccess(PublicationSuccessEvent.Factory.create(PublicationRequestEvent.Factory.create(
            "",
            10)));

        assertEquals(getCurrentValue("updates_published_total"), 1.0, 0.01);
        assertEquals(getCurrentValue("update_bytes_total"), 10.0, 0.01);
    }

    @Test
    public void onPublicationFailed() {
        final PrometheusMetricsListener listener = new PrometheusMetricsListener();

        listener.onPublicationFailed(PublicationFailedEvent.Factory.create(
            PublicationRequestEvent.Factory.create(
                "",
                10),
            ErrorReason.ACCESS_DENIED));

        assertEquals(getCurrentValue("updates_failed_total", "errorReason", "Access denied"), 1.0, 0.01);
    }

    @Test
    public void onTopicCreationRequest() {
        final PrometheusMetricsListener listener = new PrometheusMetricsListener();

        listener.onTopicCreationRequest(TopicCreationRequestEvent.Factory.create("", TopicType.JSON));
    }

    @Test
    public void onTopicCreationSuccess() {
        final PrometheusMetricsListener listener = new PrometheusMetricsListener();

        listener.onTopicCreationSuccess(TopicCreationSuccessEvent.Factory.create(TopicCreationRequestEvent.Factory.create(
            "",
            TopicType.JSON)));

        assertEquals(getCurrentValue("topics_created_total", "type", "JSON"), 1.0, 0.01);
    }

    @Test
    public void onTopicCreationFailed() {
        final PrometheusMetricsListener listener = new PrometheusMetricsListener();

        listener.onTopicCreationFailed(TopicCreationFailedEvent.Factory.create(TopicCreationRequestEvent.Factory.create(
            "",
            TopicType.JSON), TopicAddFailReason.CLUSTER_REPARTITION));

        assertEquals(getCurrentValue("topic_creation_failed_total", "failReason", "CLUSTER_REPARTITION"), 1.0, 0.01);
    }

    @Test
    public void onActive() {
        final PrometheusMetricsListener listener = new PrometheusMetricsListener();

        final EndpointConfig endpointConfig = EndpointConfig
            .builder()
            .name("endpoint-0")
            .topicPath("topic")
            .url("http://localhost/json")
            .produces("json")
            .build();

        final ServiceConfig serviceConfig = ServiceConfig
            .builder()
            .name("service")
            .host("localhost")
            .port(8080)
            .pollPeriod(60000)
            .endpoints(singletonList(endpointConfig))
            .topicPathRoot("a")
            .build();

        listener.onActive(serviceConfig);
    }

    @Test
    public void onStandbyRemove() {
        final PrometheusMetricsListener listener = new PrometheusMetricsListener();

        final EndpointConfig endpointConfig = EndpointConfig
            .builder()
            .name("endpoint-0")
            .topicPath("topic")
            .url("http://localhost/json")
            .produces("json")
            .build();

        final ServiceConfig serviceConfig = ServiceConfig
            .builder()
            .name("service")
            .host("localhost")
            .port(8080)
            .pollPeriod(60000)
            .endpoints(singletonList(endpointConfig))
            .topicPathRoot("a")
            .build();

        listener.onStandby(serviceConfig);

        assertEquals(getCurrentValue("services_current"), 1.0, 0.01);
        assertEquals(getCurrentValue("endpoints_current"), 1.0, 0.01);

        listener.onRemove(serviceConfig);

        assertEquals(getCurrentValue("services_current"), 0.0, 0.01);
        assertEquals(getCurrentValue("endpoints_current"), 0.0, 0.01);
    }

    private static double getCurrentValue(String name) {
        return CollectorRegistry.defaultRegistry.getSampleValue(name);
    }

    private static double getCurrentValue(String name, String labelName, String labelValue) {
        return CollectorRegistry.defaultRegistry.getSampleValue(
            name,
            new String[]{labelName},
            new String[]{labelValue});
    }
}
