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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.pushtechnology.adapters.rest.metrics.listeners.PollListener.PollCompletionListener;

/**
 * Unit tests for {@link DelegatingPollListener}.
 *
 * @author Matt Champion 04/06/2017
 */
public final class DelegatingPollListenerTest {
    @Mock
    private PollListener delegateListener;
    @Mock
    private PollCompletionListener delegateCompletionListener;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        when(delegateListener.onPollRequest(null, null)).thenReturn(delegateCompletionListener);
    }

    @After
    public void postCondition() {
        verifyNoMoreInteractions(delegateListener, delegateCompletionListener);
    }

    @Test
    public void delegates() {
        final PollListener pollListener = new DelegatingPollListener(delegateListener);

        final PollCompletionListener completionListener = pollListener.onPollRequest(null, null);

        verify(delegateListener).onPollRequest(null, null);

        completionListener.onPollResponse(null);

        verify(delegateCompletionListener).onPollResponse(null);

        completionListener.onPollFailure(null);

        verify(delegateCompletionListener).onPollFailure(null);
    }

}
