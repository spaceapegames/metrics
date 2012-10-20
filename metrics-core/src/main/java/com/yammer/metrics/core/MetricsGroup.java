package com.yammer.metrics.core;

import com.yammer.metrics.core.builders.*;

public class MetricsGroup {
    private final MetricsRegistry registry;
    private final Class<?> klass;

    public MetricsGroup(MetricsRegistry registry, Class<?> klass) {
        this.registry = registry;
        this.klass = klass;
    }

    public GaugeBuilder.NamedBuilder gauge(String name) {
        return registry.gauge().forClass(klass).named(name);
    }

    public CounterBuilder.NamedBuilder counter(String name) {
        return registry.counter().forClass(klass).named(name);
    }

    public HistogramBuilder.NamedBuilder histogram(String name) {
        return registry.histogram().forClass(klass).named(name);
    }

    public TimerBuilder.NamedBuilder timer(String name) {
        return registry.timer().forClass(klass).named(name);
    }

    public MeterBuilder.NamedBuilder meter(String name) {
        return registry.meter().forClass(klass).named(name);
    }
}
