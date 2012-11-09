package com.yammer.metrics;

import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.JmxReporter;
import com.yammer.metrics.stats.Sample;

import java.util.concurrent.TimeUnit;

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

    public static Timer metric(String name, Timer timer) {
        return DEFAULT_REGISTRY.add(name, timer);
    }

    public static Meter metric(String name, Meter meter) {
        return DEFAULT_REGISTRY.add(name, meter);
    }

    public static Histogram metric(String name, Histogram histogram) {
        return DEFAULT_REGISTRY.add(name, histogram);
    }

    public static Counter metric(String name, Counter counter) {
        return DEFAULT_REGISTRY.add(name, counter);
    }

    public static <T> Gauge<T> metric(String name, Gauge<T> gauge) {
        return DEFAULT_REGISTRY.add(name, gauge);
    }

    public static boolean unregister(String name) {
        return DEFAULT_REGISTRY.remove(name);
    }

    public static String name(String name, String... names) {
        final StringBuilder builder = new StringBuilder();
        append(builder, name);
        for (String s : names) {
            append(builder, s);
        }
        return builder.toString();
    }

    public static String name(Class<?> klass, String... names) {
        return name(klass.getCanonicalName(), names);
    }

    private static void append(StringBuilder builder, String part) {
        if (part != null && !part.isEmpty()) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(part);
        }
    }

    public static Counter counter() {
        return new CounterImpl();
    }

    public static Histogram histogram() {
        return histogram(Histogram.SampleType.UNIFORM);
    }

    public static Histogram histogram(HistogramImpl.SampleType type) {
        return histogram(type.newSample());
    }

    public static Histogram histogram(Sample sample) {
        return new HistogramImpl(sample);
    }

    public static Meter meter(String eventType, TimeUnit rateUnit, Clock clock) {
        return new MeterImpl(eventType, rateUnit, clock);
    }

    public static Meter meter(String eventType, TimeUnit rateUnit) {
        return meter(eventType, rateUnit, Clock.defaultClock());
    }

    public static Meter meter(String eventType) {
        return meter(eventType, TimeUnit.SECONDS);
    }

    public static Meter meter() {
        return meter("events");
    }

    public static Timer timer(TimeUnit durationUnit, TimeUnit rateUnit, Clock clock) {
        return new TimerImpl(durationUnit, rateUnit, clock);
    }

    public static Timer timer(TimeUnit durationUnit, TimeUnit rateUnit) {
        return timer(durationUnit, rateUnit, Clock.defaultClock());
    }

    public static Timer timer(TimeUnit durationUnit) {
        return timer(durationUnit, TimeUnit.SECONDS);
    }

    public static Timer timer() {
        return timer(TimeUnit.MILLISECONDS);
    }
}
