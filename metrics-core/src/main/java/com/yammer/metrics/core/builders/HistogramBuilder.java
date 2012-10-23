package com.yammer.metrics.core.builders;

import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricRegistry;

public class HistogramBuilder implements MetricBuilder<Histogram> {
    public static class BoundBuilder implements BoundMetricBuilder<Histogram> {
        private final MetricRegistry registry;
        private final String domain;
        private final String type;

        private BoundBuilder(MetricRegistry registry, String domain, String type) {
            this.registry = registry;
            this.domain = domain;
            this.type = type;
        }

        @Override
        public NamedBuilder named(String name) {
            return new NamedBuilder(registry, domain, type, name);
        }
    }

    public static class NamedBuilder implements NamedMetricBuilder<Histogram> {
        private final MetricRegistry registry;
        private final String domain;
        private final String type;
        private final String name;

        private NamedBuilder(MetricRegistry registry, String domain, String type, String name) {
            this.registry = registry;
            this.domain = domain;
            this.type = type;
            this.name = name;
        }

        @Override
        public ScopedBuilder scopedTo(String scope) {
            return new ScopedBuilder(registry, domain, type, name, scope);
        }

        public Histogram buildBiased() {
            return registry.register(new MetricName(domain, type, name),
                                     new Histogram(Histogram.SampleType.BIASED));
        }

        public Histogram buildUniform() {
            return registry.register(new MetricName(domain, type, name),
                                     new Histogram(Histogram.SampleType.UNIFORM));
        }
    }

    public static class ScopedBuilder implements ScopedMetricBuilder<Histogram> {
        private final MetricRegistry registry;
        private final String domain;
        private final String type;
        private final String name;
        private final String scope;

        private ScopedBuilder(MetricRegistry registry, String domain, String type, String name, String scope) {
            this.registry = registry;
            this.domain = domain;
            this.type = type;
            this.name = name;
            this.scope = scope;
        }

        public Histogram buildBiased() {
            return registry.register(new MetricName(domain, type, name, scope),
                                     new Histogram(Histogram.SampleType.BIASED));
        }

        public Histogram buildUniform() {
            return registry.register(new MetricName(domain, type, name, scope),
                                     new Histogram(Histogram.SampleType.UNIFORM));
        }
    }

    public static class DomainBuilder implements DomainMetricBuilder<Histogram> {
        private final MetricRegistry registry;
        private final String domain;

        private DomainBuilder(MetricRegistry registry, String domain) {
            this.registry = registry;
            this.domain = domain;
        }

        @Override
        public BoundBuilder type(String type) {
            return new BoundBuilder(registry, domain, type);
        }
    }

    public static class TypedBuilder implements TypedMetricBuilder<Histogram> {
        private final MetricRegistry registry;
        private final String type;

        private TypedBuilder(MetricRegistry registry, String type) {
            this.registry = registry;
            this.type = type;
        }

        @Override
        public BoundBuilder domain(String domain) {
            return new BoundBuilder(registry, domain, type);
        }
    }

    public static HistogramBuilder newBuilder(MetricRegistry registry) {
        return new HistogramBuilder(registry);
    }

    private final MetricRegistry registry;

    private HistogramBuilder(MetricRegistry registry) {
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
