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

import java.util.concurrent.ScheduledExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.Model;

/**
 * Unit tests for {@link RESTAdapter}.
 *
 * @author Push Technology Limited
 */
public final class RESTAdapterTest {

    @Mock
    private ScheduledExecutorService executor;
    @Mock
    private Runnable shutdownHandler;
    @Mock
    private ServiceListener listener;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(executor, shutdownHandler, listener);
    }

    @Test
    public void selfStopping() {
        final RESTAdapter adapter = new RESTAdapter(executor, shutdownHandler, listener);

        adapter.reconfigure(Model.builder().active(false).build());

        verify(shutdownHandler).run();
    }

    @Test
    public void startStopInactive() {
        final RESTAdapter adapter = new RESTAdapter(executor, shutdownHandler, listener);

        adapter.reconfigure(Model.builder().active(true).build());

        adapter.close();
    }

    @Test
    public void startRestartStopInactive() {
        final RESTAdapter adapter = new RESTAdapter(executor, shutdownHandler, listener);

        adapter.reconfigure(Model.builder().active(true).build());

        adapter.close();

        adapter.reconfigure(Model.builder().active(true).build());

        adapter.close();
    }
}
