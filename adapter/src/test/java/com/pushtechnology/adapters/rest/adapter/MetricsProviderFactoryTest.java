package com.pushtechnology.adapters.rest.adapter;

import static com.pushtechnology.adapters.rest.model.latest.MetricsConfig.Type.COUNTING;
import static com.pushtechnology.adapters.rest.model.latest.MetricsConfig.Type.SUMMARY;
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

/**
 * Unit tests for {@link MetricsProviderFactory}.
 *
 * @author Matt Champion 03/06/2017
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

        final MetricsProvider provider = factory.provide(model, executorService);

        assertNotNull(provider);
    }

    @Test
    public void createCountingProvider() throws Exception {
        final MetricsProviderFactory factory = new MetricsProviderFactory();

        final Model model = Model
            .builder()
            .metrics(MetricsConfig
                .builder()
                .type(COUNTING)
                .build())
            .build();

        final MetricsProvider provider = factory.provide(model, executorService);

        assertNotNull(provider);
    }

    @Test
    public void createSummaryProvider() throws Exception {
        final MetricsProviderFactory factory = new MetricsProviderFactory();

        final Model model = Model
            .builder()
            .metrics(MetricsConfig
                .builder()
                .type(SUMMARY)
                .build())
            .build();

        final MetricsProvider provider = factory.provide(model, executorService);

        assertNotNull(provider);
    }
}
