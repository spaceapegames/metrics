package com.yammer.metrics.core;

import com.yammer.metrics.core.builders.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A registry of metric instances.
 */
public class MetricRegistry {
    private static final int EXPECTED_METRIC_COUNT = 1024;
    private final ConcurrentMap<MetricName, Metric> metrics;
    private final List<MetricRegistryListener> listeners;
    private final String name;

    /**
     * Creates a new {@link MetricRegistry}.
     */
    public MetricRegistry() {
        this(null);
    }

    /**
     * Creates a new {@link MetricRegistry} with the given name.
     *
     * @param name  the name of the registry
     */
    public MetricRegistry(String name) {
        this.name = name;
        this.metrics = new ConcurrentHashMap<MetricName, Metric>(EXPECTED_METRIC_COUNT);
        this.listeners = new CopyOnWriteArrayList<MetricRegistryListener>();
    }

    @SuppressWarnings("unchecked")
    public <T extends Metric> T register(MetricName name, T metric) {
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

    public Metric get(MetricName name) {
        return metrics.get(name);
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
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     */
    public void unregister(MetricName name) {
        final Metric metric = metrics.remove(name);
        if (metric != null) {
            notifyMetricRemoved(name);
        }
    }

    /**
     * Adds a {@link MetricRegistryListener} to a collection of listeners that will be notified on
     * metric creation.  Listeners will be notified in the order in which they are added.
     * <p/>
     * <b>N.B.:</b> The listener will be notified of all existing metrics when it first registers.
     *
     * @param listener the listener that will be notified
     */
    public void addListener(MetricRegistryListener listener) {
        listeners.add(listener);
        for (Map.Entry<MetricName, Metric> entry : metrics.entrySet()) {
            listener.onMetricAdded(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes a {@link MetricRegistryListener} from this registry's collection of listeners.
     *
     * @param listener the listener that will be removed
     */
    public void removeListener(MetricRegistryListener listener) {
        listeners.remove(listener);
    }

    private void notifyMetricRemoved(MetricName name) {
        for (MetricRegistryListener listener : listeners) {
            listener.onMetricRemoved(name);
        }
    }

    private void notifyMetricAdded(MetricName name, Metric metric) {
        for (MetricRegistryListener listener : listeners) {
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

    public MetricGroup group(Class<?> klass) {
        return new MetricsGroupBuilder(this).forClass(klass).build();
    }
}
