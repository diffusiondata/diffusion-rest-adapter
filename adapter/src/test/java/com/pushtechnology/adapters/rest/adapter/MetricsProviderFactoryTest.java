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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.ScheduledExecutorService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.pushtechnology.adapters.rest.model.latest.MetricsConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.PrometheusConfig;

/**
 * Unit tests for {@link MetricsProviderFactory}.
 *
 * @author Push Technology Limited
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness= Strictness.LENIENT)
public final class MetricsProviderFactoryTest {
    @Mock
    private ScheduledExecutorService executorService;

    @AfterEach
    public void postConditions() {
        verifyNoMoreInteractions(executorService);
    }

    @Test
    public void createDisabledProvider() throws Exception {
        final MetricsProviderFactory factory = new MetricsProviderFactory();

        final Model model = Model
            .builder()
            .build();

        final MetricsProvider provider = factory.create(model, executorService, new MetricsDispatcher());

        assertNotNull(provider);
    }

    @Test
    public void createCountingProvider() throws Exception {
        final MetricsProviderFactory factory = new MetricsProviderFactory();

        final Model model = Model
            .builder()
            .metrics(MetricsConfig
                .builder()
                .logging(true)
                .build())
            .build();

        final MetricsProvider provider = factory.create(model, executorService, new MetricsDispatcher());

        assertNotNull(provider);
    }

    @Test
    public void createPrometheusProvider() {
        final MetricsProviderFactory factory = new MetricsProviderFactory();

        final Model model = Model
            .builder()
            .metrics(MetricsConfig
                .builder()
                .prometheus(PrometheusConfig.builder().port(9000).build())
                .build())
            .build();

        final MetricsProvider provider = factory.create(model, executorService, new MetricsDispatcher());

        assertNotNull(provider);
    }
}
