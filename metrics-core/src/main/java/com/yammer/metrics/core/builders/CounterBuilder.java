package com.yammer.metrics.core.builders;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

public class CounterBuilder implements MetricBuilder<Counter> {
    public static class BoundBuilder implements BoundMetricBuilder<Counter> {
        private final MetricsRegistry registry;
        private final String domain;
        private final String type;

        private BoundBuilder(MetricsRegistry registry, String domain, String type) {
            this.registry = registry;
            this.domain = domain;
            this.type = type;
        }

        @Override
        public NamedBuilder named(String name) {
            return new NamedBuilder(registry, domain, type, name);
        }
    }

    public static class NamedBuilder implements NamedMetricBuilder<Counter> {
        private final MetricsRegistry registry;
        private final String domain;
        private final String type;
        private final String name;

        private NamedBuilder(MetricsRegistry registry, String domain, String type, String name) {
            this.registry = registry;
            this.domain = domain;
            this.type = type;
            this.name = name;
        }

        @Override
        public ScopedBuilder scopedTo(String scope) {
            return new ScopedBuilder(registry, domain, type, name, scope);
        }

        public Counter build() {
            return registry.newCounter(new MetricName(domain, type, name));
        }
    }

    public static class ScopedBuilder implements ScopedMetricBuilder<Counter> {
        private final MetricsRegistry registry;
        private final String domain;
        private final String type;
        private final String name;
        private final String scope;

        private ScopedBuilder(MetricsRegistry registry, String domain, String type, String name, String scope) {
            this.registry = registry;
            this.domain = domain;
            this.type = type;
            this.name = name;
            this.scope = scope;
        }

        public Counter build() {
            return registry.newCounter(new MetricName(domain, type, name, scope));
        }
    }

    public static class DomainBuilder implements DomainMetricBuilder<Counter> {
        private final MetricsRegistry registry;
        private final String domain;

        private DomainBuilder(MetricsRegistry registry, String domain) {
            this.registry = registry;
            this.domain = domain;
        }

        @Override
        public BoundBuilder type(String type) {
            return new BoundBuilder(registry, domain, type);
        }
    }

    public static class TypedBuilder implements TypedMetricBuilder<Counter> {
        private final MetricsRegistry registry;
        private final String type;

        private TypedBuilder(MetricsRegistry registry, String type) {
            this.registry = registry;
            this.type = type;
        }

        @Override
        public BoundBuilder domain(String domain) {
            return new BoundBuilder(registry, domain, type);
        }
    }

    public static CounterBuilder newBuilder(MetricsRegistry registry) {
        return new CounterBuilder(registry);
    }

    private final MetricsRegistry registry;

    private CounterBuilder(MetricsRegistry registry) {
        this.registry = registry;
    }

    @Override
    public BoundBuilder forClass(Class<?> klass) {
        return new BoundBuilder(registry, klass.getPackage().getName(), klass.getSimpleName());
    }

    @Override
    public DomainBuilder domain(String domain) {
        return new DomainBuilder(registry, domain);
    }

    @Override
    public TypedBuilder type(String type) {
        return new TypedBuilder(registry, type);
    }
}
