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

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.event.listeners.PollEventListener;
import com.pushtechnology.adapters.rest.metrics.event.listeners.PublicationEventListener;
import com.pushtechnology.adapters.rest.metrics.event.listeners.ServiceEventListener;
import com.pushtechnology.adapters.rest.metrics.event.listeners.TopicCreationEventListener;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

/**
 * Metrics listener for Prometheus that updates metrics registry.
 *
 * @author Push Technology Limited
 */
public final class PrometheusMetricsListener
        implements PollEventListener, PublicationEventListener, TopicCreationEventListener, ServiceEventListener {
    private static final Counter POLL_REQUESTS = Counter
        .build()
        .name("poll_requests_total")
        .help("The number of successful poll requests.")
        .register();
    private static final Counter POLL_FAILURES = Counter
        .build()
        .name("poll_requests_failed_total")
        .labelNames("exceptionMessage")
        .help("The number of failed poll requests.")
        .register();
    private static final Counter POLL_BYTES = Counter
        .build()
        .name("polled_bytes_total")
        .help("The number of bytes returned by polls.")
        .register();
    private static final Counter POLL_DURATION = Counter
        .build()
        .name("poll_requests_duration_milliseconds")
        .help("The milliseconds to receive a poll response.")
        .register();
    private static final Counter PUBLICATION_REQUESTS = Counter
        .build()
        .name("updates_published_total")
        .help("The number of successful topic updates.")
        .register();
    private static final Counter PUBLICATION_FAILURES = Counter
        .build()
        .name("updates_failed_total")
        .labelNames("errorReason")
        .help("The number of failed topic updates")
        .register();
    private static final Counter PUBLICATION_BYTES = Counter
        .build()
        .name("update_bytes_total")
        .help("The total bytes of updates published.")
        .register();
    private static final Counter PUBLICATION_DURATION = Counter
        .build()
        .name("update_duration_milliseconds")
        .help("The milliseconds to complete a topic update.")
        .register();
    private static final Counter TOPIC_CREATION_REQUESTS = Counter
        .build()
        .name("topics_created_total")
        .labelNames("type")
        .help("The number of topics created.")
        .register();
    private static final Counter TOPIC_CREATION_FAILURES = Counter
        .build()
        .name("topic_creation_failed_total")
        .labelNames("failReason")
        .help("The number of failed attempts to create topics.")
        .register();
    private static final Counter TOPIC_CREATION_DURATION = Counter
        .build()
        .name("topic_creation_duration_milliseconds")
        .help("The milliseconds to complete the creation of a topic.")
        .register();
    private static final Gauge CURRENT_SERVICES = Gauge
        .build()
        .name("services_current")
        .help("The current number of services.")
        .register();
    private static final Gauge CURRENT_ENDPOINTS = Gauge
        .build()
        .name("endpoints_current")
        .help("The current number of endpoints.")
        .register();

    @Override
    public void onPollRequest(PollRequestEvent event) {
    }

    @Override
    public void onPollSuccess(PollSuccessEvent event) {
        POLL_REQUESTS.inc();
        POLL_BYTES.inc(event.getResponseLength());
        POLL_DURATION.inc(event.getRequestTime());
    }

    @Override
    public void onPollFailed(PollFailedEvent event) {
        POLL_FAILURES.labels(event.getException().getMessage()).inc();
    }

    @Override
    public void onPublicationRequest(PublicationRequestEvent event) {
    }

    @Override
    public void onPublicationSuccess(PublicationSuccessEvent event) {
        PUBLICATION_REQUESTS.inc();
        PUBLICATION_BYTES.inc(event.getRequestEvent().getUpdateLength());
        PUBLICATION_DURATION.inc(event.getRequestTime());
    }

    @Override
    public void onPublicationFailed(PublicationFailedEvent event) {
        PUBLICATION_FAILURES.labels(event.getErrorReason().getDescription()).inc();
    }

    @Override
    public void onTopicCreationRequest(TopicCreationRequestEvent event) {
    }

    @Override
    public void onTopicCreationSuccess(TopicCreationSuccessEvent event) {
        TOPIC_CREATION_REQUESTS.labels(event.getRequestEvent().getTopicType().toString()).inc();
        TOPIC_CREATION_DURATION.inc(event.getRequestTime());
    }

    @Override
    public void onTopicCreationFailed(TopicCreationFailedEvent event) {
        TOPIC_CREATION_FAILURES.labels(event.getFailReason().toString()).inc();
    }

    @Override
    public void onActive(ServiceConfig serviceConfig) {
    }

    @Override
    public void onStandby(ServiceConfig serviceConfig) {
        CURRENT_SERVICES.inc();
        CURRENT_ENDPOINTS.inc(serviceConfig.getEndpoints().size());
    }

    @Override
    public void onRemove(ServiceConfig serviceConfig) {
        CURRENT_SERVICES.dec();
        CURRENT_ENDPOINTS.dec(serviceConfig.getEndpoints().size());
    }
}
