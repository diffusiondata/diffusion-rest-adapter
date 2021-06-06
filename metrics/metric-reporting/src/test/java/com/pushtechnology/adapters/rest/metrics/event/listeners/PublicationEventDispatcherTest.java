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

package com.pushtechnology.adapters.rest.metrics.event.listeners;

import static com.pushtechnology.diffusion.client.callbacks.ErrorReason.ACCESS_DENIED;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.pushtechnology.adapters.rest.metrics.PublicationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PublicationSuccessEvent;

/**
 * Unit tests for {@link PublicationEventDispatcher}.
 *
 * @author Push Technology Limited
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness= Strictness.LENIENT)
public final class PublicationEventDispatcherTest {
    @Mock
    private PublicationEventListener publicationEventListener;
    @Captor
    private ArgumentCaptor<PublicationRequestEvent> requestCaptor;
    @Captor
    private ArgumentCaptor<PublicationSuccessEvent> successCaptor;
    @Captor
    private ArgumentCaptor<PublicationFailedEvent> failedCaptor;

    @AfterEach
    public void postConditions() {
        verifyNoMoreInteractions(publicationEventListener);
    }

    @Test
    public void onPublicationRequest() throws Exception {
        final PublicationEventDispatcher dispatcher = new PublicationEventDispatcher(publicationEventListener);

        dispatcher.onPublicationRequest("service/endpoint", 10);

        verify(publicationEventListener).onPublicationRequest(requestCaptor.capture());
    }

    @Test
    public void onPublication() throws Exception {
        final PublicationEventDispatcher dispatcher = new PublicationEventDispatcher(publicationEventListener);

        dispatcher.onPublicationRequest("service/endpoint", 10).onPublication();

        final PublicationRequestEvent requestEvent =
            PublicationRequestEvent.Factory.create("service/endpoint", 10);
        verify(publicationEventListener).onPublicationRequest(requestCaptor.capture());
        verify(publicationEventListener).onPublicationSuccess(successCaptor.capture());
    }

    @Test
    public void onPublicationFailed() throws Exception {
        final PublicationEventDispatcher dispatcher = new PublicationEventDispatcher(publicationEventListener);

        dispatcher.onPublicationRequest("service/endpoint", 10).onPublicationFailed(ACCESS_DENIED);

        final PublicationRequestEvent requestEvent =
            PublicationRequestEvent.Factory.create("service/endpoint", 10);
        verify(publicationEventListener).onPublicationRequest(requestCaptor.capture());
        verify(publicationEventListener).onPublicationFailed(failedCaptor.capture());
    }
}
