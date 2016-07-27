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

package com.pushtechnology.adapters.rest.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.diffusion.client.session.Session;

/**
 * Unit tests for {@link SessionWrapper}.
 *
 * @author Push Technology Limited
 */
public final class SessionWrapperTest {
    @Mock
    private Session session;

    private SessionWrapper sessionWrapper;

    @Before
    public void setUp() {
        initMocks(this);

        sessionWrapper = new SessionWrapper(session);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(session);
    }

    @Test
    public void close() {
        sessionWrapper.close();

        verify(session).close();
    }
}
