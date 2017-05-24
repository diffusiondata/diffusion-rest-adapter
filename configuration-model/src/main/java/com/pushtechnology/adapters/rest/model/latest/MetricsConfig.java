package com.pushtechnology.adapters.rest.model.latest;

import static com.pushtechnology.adapters.rest.model.latest.MetricsConfig.Type.OFF;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Metrics configuration. Version 14.
 * <p>
 * Description of the metrics to gather and report.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
public final class MetricsConfig {
    /**
     * Type of metrics. Defaults to OFF.
     */
    @Builder.Default
    Type type = OFF;

    /**
     * The type of metrics.
     */
    public enum Type {
        OFF,
        COUNTING
    }
}