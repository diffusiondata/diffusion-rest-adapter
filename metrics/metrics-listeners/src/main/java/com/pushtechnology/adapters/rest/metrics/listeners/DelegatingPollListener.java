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

package com.pushtechnology.adapters.rest.metrics.listeners;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpResponse;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Delegating implementation of {@link PollListener}.
 *
 * @author Matt Champion 04/06/2017
 */
public final class DelegatingPollListener implements PollListener {
    private final Collection<PollListener> delegates;

    /**
     * Constructor.
     */
    public DelegatingPollListener(PollListener... delegates) {
        this.delegates = asList(delegates);
    }

    /**
     * Constructor.
     */
    public DelegatingPollListener(Collection<PollListener> delegates) {
        this.delegates = new ArrayList<>();
        this.delegates.addAll(delegates);
    }

    @Override
    public PollCompletionListener onPollRequest(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        final List<PollCompletionListener> completionListeners = new ArrayList<>();
        delegates.forEach(delegate -> {
            completionListeners.add(delegate.onPollRequest(serviceConfig, endpointConfig));
        });
        return new DelegatingCompletionListener(completionListeners);
    }

    private static final class DelegatingCompletionListener implements PollCompletionListener {
        private final Collection<PollCompletionListener> delegates;

        private DelegatingCompletionListener(Collection<PollCompletionListener> delegates) {
            this.delegates = delegates;
        }

        @Override
        public void onPollResponse(HttpResponse response) {
            delegates.forEach(delegate -> delegate.onPollResponse(response));
        }

        @Override
        public void onPollFailure(Exception exception) {
            delegates.forEach(delegate -> delegate.onPollFailure(exception));
        }
    }
}
