/*******************************************************************************
 * Copyright (C) 2020 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.metrics.event.listeners;

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;
import com.pushtechnology.adapters.rest.metrics.listeners.PollListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;

/**
 * Listener for events about polling.
 *
 * @author Push Technology Limited
 */
public final class PollEventDispatcher implements PollListener {
    private final PollEventListener pollEventListener;

    /**
     * Constructor.
     */
    public PollEventDispatcher(PollEventListener pollEventListener) {
        this.pollEventListener = pollEventListener;
    }

    @Override
    public PollCompletionListener onPollRequest(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        final PollRequestEvent pollRequestEvent = PollRequestEvent.Factory.create(
            (serviceConfig.isSecure() ? "https://" : "http://") +
                serviceConfig.getHost() +
                ":" +
                serviceConfig.getPort() +
                endpointConfig.getUrl());

        pollEventListener.onPollRequest(pollRequestEvent);

        return new CompletionListener(pollRequestEvent, pollEventListener);
    }

    /**
     * Implementation of {@link PollListener.PollCompletionListener} that notifies a
     * {@link PollEventListener} of events.
     */
    private static final class CompletionListener implements PollListener.PollCompletionListener {
        private final PollRequestEvent pollRequestEvent;
        private final PollEventListener pollEventListener;

        private CompletionListener(PollRequestEvent pollRequestEvent, PollEventListener pollEventListener) {
            this.pollRequestEvent = pollRequestEvent;
            this.pollEventListener = pollEventListener;
        }

        @Override
        public void onPollResponse(EndpointResponse response) {
            pollEventListener.onPollSuccess(PollSuccessEvent.Factory.create(
                pollRequestEvent,
                response.getStatusCode(),
                response.getResponseLength()));
        }

        @Override
        public void onPollFailure(Exception exception) {
            pollEventListener.onPollFailed(PollFailedEvent.Factory.create(pollRequestEvent, exception));
        }
    }
}
