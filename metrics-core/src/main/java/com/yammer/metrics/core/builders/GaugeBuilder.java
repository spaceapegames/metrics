package com.yammer.metrics.core.builders;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

public class GaugeBuilder implements MetricBuilder<Gauge<?>> {
    public static class BoundBuilder implements BoundMetricBuilder<Gauge<?>> {
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

    public static class NamedBuilder implements NamedMetricBuilder<Gauge<?>> {
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

        public <T, G extends Gauge<T>> G build(G gauge) {
            return registry.register(new MetricName(domain, type, name), gauge);
        }
    }

    public static class ScopedBuilder implements ScopedMetricBuilder<Gauge<?>> {
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

        public Gauge<?> build(Gauge<?> gauge) {
            return registry.register(new MetricName(domain, type, name, scope), gauge);
        }
    }

    public static class DomainBuilder implements DomainMetricBuilder<Gauge<?>> {
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

    public static class TypedBuilder implements TypedMetricBuilder<Gauge<?>> {
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

    public static GaugeBuilder newBuilder(MetricsRegistry registry) {
        return new GaugeBuilder(registry);
    }

    private final MetricsRegistry registry;

    private GaugeBuilder(MetricsRegistry registry) {
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
