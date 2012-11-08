package com.yammer.metrics.core;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A registry of metric instances.
 */
public class MetricsRegistry implements Iterable<Map.Entry<String, Metric>> {
    private final ConcurrentMap<String, Metric> metrics;
    private final List<MetricsRegistryListener> listeners;
    private final String name;

    /**
     * Creates a new {@link MetricsRegistry}.
     */
    public MetricsRegistry() {
        this(null);
    }

    /**
     * Creates a new {@link MetricsRegistry} with the given name and {@link Clock} instance.
     *
     * @param name     the name of the registry
     */
    public MetricsRegistry(String name) {
        this.name = name;
        this.metrics = new ConcurrentSkipListMap<String, Metric>();
        this.listeners = new CopyOnWriteArrayList<MetricsRegistryListener>();
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
    public <T extends Metric> T add(String name, T metric) {
        final Metric existingMetric = metrics.get(name);
        if (existingMetric == null) {
            final Metric justAddedMetric = metrics.putIfAbsent(name, metric);
            if (justAddedMetric == null) {
                notifyMetricRegistered(name, metric);
                return metric;
            }
            return (T) justAddedMetric;
        }
        return (T) existingMetric;
    }

    /**
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     * @return {@code true} if the metric was removed; {@code false} otherwise
     */
    public boolean remove(String name) {
        final Metric metric = metrics.remove(name);
        if (metric != null) {
            notifyMetricUnregistered(name);
            return true;
        }
        return false;
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
        for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
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

    private void notifyMetricUnregistered(String name) {
        for (MetricsRegistryListener listener : listeners) {
            listener.onMetricRemoved(name);
        }
    }

    private void notifyMetricRegistered(String name, Metric metric) {
        for (MetricsRegistryListener listener : listeners) {
            listener.onMetricAdded(name, metric);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public Iterator<Map.Entry<String, Metric>> iterator() {
        final Iterator<Map.Entry<String, Metric>> iterator = metrics.entrySet().iterator();
        return new Iterator<Map.Entry<String, Metric>>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Map.Entry<String, Metric> next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Iterable<Map.Entry<String, Metric>> filter(final MetricPredicate predicate) {
        return new Iterable<Map.Entry<String, Metric>>() {
            @Override
            public Iterator<Map.Entry<String, Metric>> iterator() {
                final Iterator<Map.Entry<String, Metric>> iterator = MetricsRegistry.this.iterator();
                return new Iterator<Map.Entry<String, Metric>>() {
                    private Map.Entry<String, Metric> element;

                    @Override
                    public boolean hasNext() {
                        if (element != null) {
                            return true;
                        }

                        while (iterator.hasNext()) {
                            final Map.Entry<String, Metric> possibleElement = iterator.next();
                            if (predicate.matches(possibleElement.getKey(), possibleElement.getValue())) {
                                this.element = possibleElement;
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public Map.Entry<String, Metric> next() {
                        if (hasNext()) {
                            final Map.Entry<String, Metric> e = element;
                            this.element = null;
                            return e;
                        }
                        throw new NoSuchElementException();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}
