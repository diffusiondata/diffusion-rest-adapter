package com.pushtechnology.adapters.rest.model.latest;

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
     * Type of metrics.
     */
    Type type;

    public enum Type {
        OFF,
        COUNTING
    }
}
