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
import com.pushtechnology.adapters.rest.model.latest.SummaryConfig;
import com.pushtechnology.diffusion.client.session.Session;

/**
 * Unit tests for {@link MetricsProviderFactory}.
 *
 * @author Push Technology Limited
 */
public final class MetricsProviderFactoryTest {
    @Mock
    private Session session;
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

        final MetricsProvider provider = factory.create(session, model, executorService, new MetricsDispatcher());

        assertNotNull(provider);
    }

    @Test
    public void createCountingProvider() throws Exception {
        final MetricsProviderFactory factory = new MetricsProviderFactory();

        final Model model = Model
            .builder()
            .metrics(MetricsConfig
                .builder()
                .counting(true)
                .build())
            .build();

        final MetricsProvider provider = factory.create(session, model, executorService, new MetricsDispatcher());

        assertNotNull(provider);
    }

    @Test
    public void createSummaryProvider() throws Exception {
        final MetricsProviderFactory factory = new MetricsProviderFactory();

        final Model model = Model
            .builder()
            .metrics(MetricsConfig
                .builder()
                .summary(SummaryConfig.builder().eventBound(10).build())
                .build())
            .build();

        final MetricsProvider provider = factory.create(session, model, executorService, new MetricsDispatcher());

        assertNotNull(provider);
    }
}
