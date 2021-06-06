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

package com.pushtechnology.adapters.rest.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for {@link MetricsProvider}.
 *
 * @author Push Technology Limited
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness=Strictness.LENIENT)
public final class MetricsProviderTest {
    @Mock
    private Runnable startTask;
    @Mock
    private Runnable stopTask;

    @AfterEach
    public void postConditions() {
        verifyNoMoreInteractions(startTask, stopTask);
    }

    @Test
    public void start() throws Exception {
        final MetricsProvider metricsProvider = new MetricsProvider(
            startTask,
            stopTask);

        metricsProvider.start();

        verify(startTask).run();
    }

    @Test
    public void close() throws Exception {
        final MetricsProvider metricsProvider = new MetricsProvider(
            startTask,
            stopTask);

        metricsProvider.close();

        verify(stopTask).run();
    }
}