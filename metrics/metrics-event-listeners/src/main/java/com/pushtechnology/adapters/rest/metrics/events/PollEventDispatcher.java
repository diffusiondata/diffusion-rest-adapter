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

package com.pushtechnology.adapters.rest.metrics.events;

import static java.lang.System.currentTimeMillis;

import org.apache.http.HttpResponse;

import com.pushtechnology.adapters.rest.metrics.PollFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PollListener;
import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Listener for events about polling.
 *
 * @author Matt Champion 17/05/2017
 */
/*package*/ final class PollEventDispatcher implements PollListener {
    private final PollEventListener pollEventListener;

    /**
     * Constructor.
     */
    /*package*/ PollEventDispatcher(PollEventListener pollEventListener) {
        this.pollEventListener = pollEventListener;
    }

    @Override
    public PollCompletionListener onPollRequest(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        final PollRequestEvent pollRequestEvent = new PollRequestEvent(
            (serviceConfig.isSecure() ? "https://" : "http://") +
                serviceConfig.getHost() +
                ":" +
                serviceConfig.getPort() +
                endpointConfig.getUrl(),
            currentTimeMillis());

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
        public void onPollResponse(HttpResponse response) {
            pollEventListener.onPollSuccess(new PollSuccessEvent(
                pollRequestEvent,
                response.getStatusLine().getStatusCode(),
                response.getEntity().getContentLength(),
                currentTimeMillis()));
        }

        @Override
        public void onPollFailure(Exception exception) {
            pollEventListener.onPollFailed(new PollFailedEvent(pollRequestEvent, exception, currentTimeMillis()));
        }
    }
}
