package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.core.MetricRegistry;

/**
 * The base class for all metric reporters.
 */
public abstract class AbstractReporter {
    private final MetricRegistry metricRegistry;

    /**
     * Creates a new {@link AbstractReporter} instance.
     *
     * @param registry    the {@link com.yammer.metrics.core.MetricRegistry} containing the metrics this reporter will
     *                    report
     */
    protected AbstractReporter(MetricRegistry registry) {
        this.metricRegistry = registry;
    }

    /**
     * Stops the reporter and closes any internal resources.
     */
    public void shutdown() {
        // nothing to do here
    }

    /**
     * Returns the reporter's {@link com.yammer.metrics.core.MetricRegistry}.
     *
     * @return the reporter's {@link com.yammer.metrics.core.MetricRegistry}
     */
    protected MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}
