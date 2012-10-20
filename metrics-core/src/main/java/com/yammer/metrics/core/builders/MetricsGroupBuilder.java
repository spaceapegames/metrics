package com.yammer.metrics.core.builders;

import com.yammer.metrics.core.MetricsGroup;
import com.yammer.metrics.core.MetricsRegistry;

public class MetricsGroupBuilder {
    public static class BoundMetricsGroupBuilder {
        private final MetricsRegistry registry;
        private final Class<?> klass;

        BoundMetricsGroupBuilder(MetricsRegistry registry, Class<?> klass) {
            this.registry = registry;
            this.klass = klass;
        }

        public MetricsGroup build() {
            return new MetricsGroup(registry, klass);
        }
    }

    private final MetricsRegistry registry;

    public MetricsGroupBuilder(MetricsRegistry registry) {
        this.registry = registry;
    }

    public BoundMetricsGroupBuilder forClass(Class<?> klass) {
        return new BoundMetricsGroupBuilder(registry, klass);
    }
}
