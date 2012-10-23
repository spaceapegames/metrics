package com.yammer.metrics;

import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.reporting.JmxReporter;

/**
 * A default metrics registry.
 */
public class Metrics {
    private static final MetricRegistry DEFAULT_REGISTRY = new MetricRegistry();
    private static final JmxReporter JMX_REPORTER = new JmxReporter(DEFAULT_REGISTRY);

    static {
        JMX_REPORTER.start();
    }

    private Metrics() { /* unused */ }

    /**
     * Returns the (static) default registry.
     *
     * @return the metrics registry
     */
    public static MetricRegistry defaultRegistry() {
        return DEFAULT_REGISTRY;
    }
}
