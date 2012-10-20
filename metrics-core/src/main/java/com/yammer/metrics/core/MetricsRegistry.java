package com.yammer.metrics.core;

import com.yammer.metrics.core.Histogram.SampleType;
import com.yammer.metrics.core.builders.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * A registry of metric instances.
 */
public class MetricsRegistry {
    private static final int EXPECTED_METRIC_COUNT = 1024;
    private final Clock clock;
    private final ConcurrentMap<MetricName, Metric> metrics;
    private final List<MetricsRegistryListener> listeners;
    private final String name;

    /**
     * Creates a new {@link MetricsRegistry}.
     */
    public MetricsRegistry() {
        this(Clock.defaultClock());
    }

    /**
     * Creates a new {@link MetricsRegistry} with the given name.
     *
     * @param name the name of the registry
     */
    public MetricsRegistry(String name) {
        this(name, Clock.defaultClock());
    }

    /**
     * Creates a new {@link MetricsRegistry} with the given {@link Clock} instance.
     *
     * @param clock a {@link Clock} instance
     */
    public MetricsRegistry(Clock clock) {
        this(null, clock);
    }

    /**
     * Creates a new {@link MetricsRegistry} with the given name and {@link Clock} instance.
     *
     * @param name  the name of the registry
     * @param clock a {@link Clock} instance
     */
    public MetricsRegistry(String name, Clock clock) {
        this.name = name;
        this.clock = clock;
        this.metrics = new ConcurrentHashMap<MetricName, Metric>(EXPECTED_METRIC_COUNT);
        this.listeners = new CopyOnWriteArrayList<MetricsRegistryListener>();
    }

    /**
     * Given a new {@link Gauge}, registers it under the given metric name.
     *
     * @param metricName the name of the metric
     * @param gauge      the metric
     * @param <T>        the type of the value returned by the metric
     * @return {@code metric}
     */
    public <T, G extends Gauge<T>> G newGauge(MetricName metricName,
                                              G gauge) {
        return getOrAdd(metricName, gauge);
    }

    /**
     * Creates a new {@link Counter} and registers it under the given metric name.
     *
     * @param metricName the name of the metric
     * @return a new {@link Counter}
     */
    public Counter newCounter(MetricName metricName) {
        return getOrAdd(metricName, new Counter());
    }

    /**
     * Creates a new {@link Histogram} and registers it under the given metric name.
     *
     * @param metricName the name of the metric
     * @param biased     whether or not the histogram should be biased
     * @return a new {@link Histogram}
     */
    public Histogram newHistogram(MetricName metricName,
                                  boolean biased) {
        return getOrAdd(metricName,
                        new Histogram(biased ? SampleType.BIASED : SampleType.UNIFORM));
    }

    /**
     * Creates a new {@link Meter} and registers it under the given metric name.
     *
     * @param metricName the name of the metric
     * @param eventType  the plural name of the type of events the meter is measuring (e.g., {@code
     *                   "requests"})
     * @param unit       the rate unit of the new meter
     * @return a new {@link Meter}
     */
    public Meter newMeter(MetricName metricName,
                          String eventType,
                          TimeUnit unit) {
        final Metric existingMetric = metrics.get(metricName);
        if (existingMetric != null) {
            return (Meter) existingMetric;
        }
        return getOrAdd(metricName, new Meter(eventType, unit, clock));
    }

    /**
     * Creates a new {@link Timer} and registers it under the given metric name.
     *
     * @param metricName   the name of the metric
     * @param durationUnit the duration scale unit of the new timer
     * @param rateUnit     the rate scale unit of the new timer
     * @return a new {@link Timer}
     */
    public Timer newTimer(MetricName metricName,
                          TimeUnit durationUnit,
                          TimeUnit rateUnit) {
        final Metric existingMetric = metrics.get(metricName);
        if (existingMetric != null) {
            return (Timer) existingMetric;
        }
        return getOrAdd(metricName,
                        new Timer(durationUnit, rateUnit, clock));
    }

    /**
     * Returns an unmodifiable map of all metrics and their names.
     *
     * @return an unmodifiable map of all metrics and their names
     */
    public Map<MetricName, Metric> getAllMetrics() {
        return Collections.unmodifiableMap(metrics);
    }

    /**
     * Returns a grouped and sorted map of all registered metrics.
     *
     * @return all registered metrics, grouped by name and sorted
     */
    public SortedMap<String, SortedMap<MetricName, Metric>> getGroupedMetrics() {
        return getGroupedMetrics(MetricPredicate.ALL);
    }

    /**
     * Returns a grouped and sorted map of all registered metrics which match then given {@link
     * MetricPredicate}.
     *
     * @param predicate a predicate which metrics have to match to be in the results
     * @return all registered metrics which match {@code predicate}, sorted by name
     */
    public SortedMap<String, SortedMap<MetricName, Metric>> getGroupedMetrics(MetricPredicate predicate) {
        final SortedMap<String, SortedMap<MetricName, Metric>> groups =
                new TreeMap<String, SortedMap<MetricName, Metric>>();
        for (Map.Entry<MetricName, Metric> entry : metrics.entrySet()) {
            final String qualifiedTypeName = entry.getKey().getDomain() + "." + entry.getKey()
                                                                                     .getType();
            if (predicate.matches(entry.getKey(), entry.getValue())) {
                final String scopedName;
                if (entry.getKey().hasScope()) {
                    scopedName = qualifiedTypeName + "." + entry.getKey().getScope();
                } else {
                    scopedName = qualifiedTypeName;
                }
                SortedMap<MetricName, Metric> group = groups.get(scopedName);
                if (group == null) {
                    group = new TreeMap<MetricName, Metric>();
                    groups.put(scopedName, group);
                }
                group.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.unmodifiableSortedMap(groups);
    }

    /**
     * Removes the metric for the given class with the given name.
     *
     * @param klass the klass the metric is associated with
     * @param name  the name of the metric
     */
    public void removeMetric(Class<?> klass,
                             String name) {
        removeMetric(klass, name, null);
    }

    /**
     * Removes the metric for the given class with the given name and scope.
     *
     * @param klass the klass the metric is associated with
     * @param name  the name of the metric
     * @param scope the scope of the metric
     */
    public void removeMetric(Class<?> klass,
                             String name,
                             String scope) {
        removeMetric(new MetricName(klass, name, scope));
    }

    /**
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     */
    public void removeMetric(MetricName name) {
        final Metric metric = metrics.remove(name);
        if (metric != null) {
            notifyMetricRemoved(name);
        }
    }

    /**
     * Adds a {@link MetricsRegistryListener} to a collection of listeners that will be notified on
     * metric creation.  Listeners will be notified in the order in which they are added.
     * <p/>
     * <b>N.B.:</b> The listener will be notified of all existing metrics when it first registers.
     *
     * @param listener the listener that will be notified
     */
    public void addListener(MetricsRegistryListener listener) {
        listeners.add(listener);
        for (Map.Entry<MetricName, Metric> entry : metrics.entrySet()) {
            listener.onMetricAdded(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes a {@link MetricsRegistryListener} from this registry's collection of listeners.
     *
     * @param listener the listener that will be removed
     */
    public void removeListener(MetricsRegistryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Gets any existing metric with the given name or, if none exists, adds the given metric.
     *
     * @param name   the metric's name
     * @param metric the new metric
     * @param <T>    the type of the metric
     * @return either the existing metric or {@code metric}
     */
    @SuppressWarnings("unchecked")
    protected <T extends Metric> T getOrAdd(MetricName name, T metric) {
        final Metric existingMetric = metrics.get(name);
        if (existingMetric == null) {
            final Metric justAddedMetric = metrics.putIfAbsent(name, metric);
            if (justAddedMetric == null) {
                notifyMetricAdded(name, metric);
                return metric;
            }
            return (T) justAddedMetric;
        }
        return (T) existingMetric;
    }

    private void notifyMetricRemoved(MetricName name) {
        for (MetricsRegistryListener listener : listeners) {
            listener.onMetricRemoved(name);
        }
    }

    private void notifyMetricAdded(MetricName name, Metric metric) {
        for (MetricsRegistryListener listener : listeners) {
            listener.onMetricAdded(name, metric);
        }
    }

    public String getName() {
        return name;
    }

    public GaugeBuilder gauge() {
        return GaugeBuilder.newBuilder(this);
    }

    public CounterBuilder counter() {
        return CounterBuilder.newBuilder(this);
    }

    public HistogramBuilder histogram() {
        return HistogramBuilder.newBuilder(this);
    }

    public MeterBuilder meter() {
        return MeterBuilder.newBuilder(this);
    }

    public TimerBuilder timer() {
        return TimerBuilder.newBuilder(this);
    }

    public MetricsGroup group(Class<?> klass) {
        return new MetricsGroupBuilder(this).forClass(klass).build();
    }
}
