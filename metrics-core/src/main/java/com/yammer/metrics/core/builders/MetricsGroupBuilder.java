package com.yammer.metrics.core.builders;

import com.yammer.metrics.core.MetricGroup;
import com.yammer.metrics.core.MetricRegistry;

public class MetricsGroupBuilder {
    public static class BoundMetricsGroupBuilder {
        private final MetricRegistry registry;
        private final Class<?> klass;

        BoundMetricsGroupBuilder(MetricRegistry registry, Class<?> klass) {
            this.registry = registry;
            this.klass = klass;
        }

        public MetricGroup build() {
            return new MetricGroup(registry, klass);
        }
    }

    private final MetricRegistry registry;

    public MetricsGroupBuilder(MetricRegistry registry) {
        this.registry = registry;
    }

    public BoundMetricsGroupBuilder forClass(Class<?> klass) {
        return new BoundMetricsGroupBuilder(registry, klass);
    }
}
