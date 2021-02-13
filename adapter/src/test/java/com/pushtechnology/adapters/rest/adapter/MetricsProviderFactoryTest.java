package com.pushtechnology.adapters.rest.adapter;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.ScheduledExecutorService;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.pushtechnology.adapters.rest.model.latest.MetricsConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.PrometheusConfig;

/**
 * Unit tests for {@link MetricsProviderFactory}.
 *
 * @author Push Technology Limited
 */
public final class MetricsProviderFactoryTest {
    @Mock
    private ScheduledExecutorService executorService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @After
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
