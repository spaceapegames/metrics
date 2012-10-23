package com.yammer.metrics.core.builders;

import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricRegistry;

import java.util.concurrent.TimeUnit;

public class MeterBuilder implements MetricBuilder<Meter> {
    public static class BoundBuilder implements BoundMetricBuilder<Meter> {
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

    public static class NamedBuilder implements NamedMetricBuilder<Meter> {
        private final MetricRegistry registry;
        private final String domain;
        private final String type;
        private final String name;
        private String eventName;
        private TimeUnit rateUnit;

        private NamedBuilder(MetricRegistry registry, String domain, String type, String name) {
            this.registry = registry;
            this.domain = domain;
            this.type = type;
            this.name = name;
            this.eventName = "events";
            this.rateUnit = TimeUnit.SECONDS;
        }

        @Override
        public ScopedBuilder scopedTo(String scope) {
            return new ScopedBuilder(registry, domain, type, name, scope);
        }

        public NamedBuilder measuring(String eventName) {
            this.eventName = eventName;
            return this;
        }

        public NamedBuilder per(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public Meter build() {
            return registry.register(new MetricName(domain, type, name),
                                     new Meter(eventName, rateUnit));
        }
    }

    public static class ScopedBuilder implements ScopedMetricBuilder<Meter> {
        private final MetricRegistry registry;
        private final String domain;
        private final String type;
        private final String name;
        private final String scope;
        private String eventName;
        private TimeUnit rateUnit;


        private ScopedBuilder(MetricRegistry registry, String domain, String type, String name, String scope) {
            this.registry = registry;
            this.domain = domain;
            this.type = type;
            this.name = name;
            this.scope = scope;
            this.eventName = "events";
            this.rateUnit = TimeUnit.SECONDS;
        }

        public ScopedBuilder measuring(String eventName) {
            this.eventName = eventName;
            return this;
        }

        public ScopedBuilder per(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public Meter build() {
            return registry.register(new MetricName(domain, type, name, scope), new Meter(eventName,
                                                                                          rateUnit));
        }
    }

    public static class DomainBuilder implements DomainMetricBuilder<Meter> {
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

    public static class TypedBuilder implements TypedMetricBuilder<Meter> {
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

    public static MeterBuilder newBuilder(MetricRegistry registry) {
        return new MeterBuilder(registry);
    }

    private final MetricRegistry registry;

    private MeterBuilder(MetricRegistry registry) {
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
