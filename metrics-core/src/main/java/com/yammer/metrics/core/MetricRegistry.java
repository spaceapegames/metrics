package com.yammer.metrics.core;

import com.yammer.metrics.util.FilteredIterator;
import com.yammer.metrics.util.UnmodifiableIterator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A registry of metric instances.
 */
public class MetricRegistry implements Iterable<Map.Entry<String, Metric>> {
    private final ConcurrentMap<String, Metric> metrics;
    private final List<MetricRegistryListener> listeners;
    private final String name;

    /**
     * Creates a new {@link MetricRegistry}.
     */
    public MetricRegistry() {
        this(null);
    }

    /**
     * Creates a new {@link MetricRegistry} with the given name and {@link Clock} instance.
     *
     * @param name     the name of the registry
     */
    public MetricRegistry(String name) {
        this.name = name;
        this.metrics = new ConcurrentSkipListMap<String, Metric>();
        this.listeners = new CopyOnWriteArrayList<MetricRegistryListener>();
    }

    @SuppressWarnings("unchecked")
    public <T> Gauge<T> gauge(String name, Gauge<T> gauge) {
        final Metric existingMetric = metrics.putIfAbsent(name, gauge);
        if (existingMetric == null) {
            notifyMetricRegistered(name, gauge);
            return gauge;
        }
        return (Gauge<T>) existingMetric;
    }

    public Counter counter(String name) {
        final Metric existingMetric = metrics.get(name);
        if (existingMetric == null) {
            final Counter counter = new CounterImpl();
            final Metric justAddedMetric = metrics.putIfAbsent(name, counter);
            if (justAddedMetric == null) {
                notifyMetricRegistered(name, counter);
                return counter;
            }
            return (Counter) justAddedMetric;
        }
        return (Counter) existingMetric;
    }

    public Meter meter(String name) {
        final Metric existingMetric = metrics.get(name);
        if (existingMetric == null) {
            final Meter meter = new MeterImpl(Clock.defaultClock());
            final Metric justAddedMetric = metrics.putIfAbsent(name, meter);
            if (justAddedMetric == null) {
                notifyMetricRegistered(name, meter);
                return meter;
            }
            return (Meter) justAddedMetric;
        }
        return (Meter) existingMetric;
    }

    public Histogram histogram(String name) {
        final Metric existingMetric = metrics.get(name);
        if (existingMetric == null) {
            final Histogram histogram = new HistogramImpl(Histogram.SampleType.UNIFORM);
            final Metric justAddedMetric = metrics.putIfAbsent(name, histogram);
            if (justAddedMetric == null) {
                notifyMetricRegistered(name, histogram);
                return histogram;
            }
            return (Histogram) justAddedMetric;
        }
        return (Histogram) existingMetric;
    }

    public Timer timer(String name) {
        final Metric existingMetric = metrics.get(name);
        if (existingMetric == null) {
            final Timer timer = new TimerImpl(Clock.defaultClock());
            final Metric justAddedMetric = metrics.putIfAbsent(name, timer);
            if (justAddedMetric == null) {
                notifyMetricRegistered(name, timer);
                return timer;
            }
            return (Timer) justAddedMetric;
        }
        return (Timer) existingMetric;
    }
    
    public boolean add(String name, Metric metric) {
        return metrics.putIfAbsent(name, metric) == null;
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
     * Adds a {@link MetricRegistryListener} to a collection of listeners that will be notified on
     * metric creation.  Listeners will be notified in the order in which they are added.
     * <p/>
     * <b>N.B.:</b> The listener will be notified of all existing metrics when it first registers.
     *
     * @param listener the listener that will be notified
     */
    public void addListener(MetricRegistryListener listener) {
        listeners.add(listener);
        for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
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

    private void notifyMetricUnregistered(String name) {
        for (MetricRegistryListener listener : listeners) {
            listener.onMetricRemoved(name);
        }
    }

    private void notifyMetricRegistered(String name, Metric metric) {
        for (MetricRegistryListener listener : listeners) {
            listener.onMetricAdded(name, metric);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public Iterator<Map.Entry<String, Metric>> iterator() {
        return new UnmodifiableIterator<Map.Entry<String, Metric>>(metrics.entrySet().iterator());
    }

    public Iterable<Map.Entry<String, Metric>> filter(final MetricPredicate predicate) {
        return new Iterable<Map.Entry<String, Metric>>() {
            @Override
            public Iterator<Map.Entry<String, Metric>> iterator() {
                return new FilteredIterator<Map.Entry<String, Metric>>(MetricRegistry.this.iterator()) {
                    @Override
                    protected boolean matches(Map.Entry<String, Metric> possibleElement) {
                        return predicate.matches(possibleElement.getKey(),
                                                 possibleElement.getValue());
                    }
                };
            }
        };
    }
}
