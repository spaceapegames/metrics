package com.yammer.metrics.core.builders;

import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

import java.util.concurrent.TimeUnit;

public class TimerBuilder implements MetricBuilder<Timer> {
    public static class BoundBuilder implements BoundMetricBuilder<Timer> {
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

    public static class NamedBuilder implements NamedMetricBuilder<Timer> {
        private final MetricsRegistry registry;
        private final String domain;
        private final String type;
        private final String name;
        private TimeUnit durationUnit;
        private TimeUnit rateUnit;

        private NamedBuilder(MetricsRegistry registry, String domain, String type, String name) {
            this.registry = registry;
            this.domain = domain;
            this.type = type;
            this.name = name;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.rateUnit = TimeUnit.SECONDS;
        }

        @Override
        public ScopedBuilder scopedTo(String scope) {
            return new ScopedBuilder(registry, domain, type, name, scope);
        }

        public NamedBuilder measuring(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        public NamedBuilder per(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public Timer build() {
            return registry.newTimer(new MetricName(domain, type, name), durationUnit, rateUnit);
        }
    }

    public static class ScopedBuilder implements ScopedMetricBuilder<Timer> {
        private final MetricsRegistry registry;
        private final String domain;
        private final String type;
        private final String name;
        private final String scope;
        private TimeUnit durationUnit;
        private TimeUnit rateUnit;

        private ScopedBuilder(MetricsRegistry registry, String domain, String type, String name, String scope) {
            this.registry = registry;
            this.domain = domain;
            this.type = type;
            this.name = name;
            this.scope = scope;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.rateUnit = TimeUnit.SECONDS;
        }

        public ScopedBuilder measuring(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        public ScopedBuilder per(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public Timer build() {
            return registry.newTimer(new MetricName(domain, type, name, scope),
                                     durationUnit,
                                     rateUnit);
        }
    }

    public static class DomainBuilder implements DomainMetricBuilder<Timer> {
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

    public static class TypedBuilder implements TypedMetricBuilder<Timer> {
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

    public static TimerBuilder newBuilder(MetricsRegistry registry) {
        return new TimerBuilder(registry);
    }

    private final MetricsRegistry registry;

    private TimerBuilder(MetricsRegistry registry) {
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
