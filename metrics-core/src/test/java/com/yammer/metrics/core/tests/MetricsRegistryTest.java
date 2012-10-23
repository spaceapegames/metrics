package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.*;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.SortedMap;
import java.util.TreeMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class MetricsRegistryTest {
    private final MetricsRegistry registry = new MetricsRegistry();

    @Test
    public void sortingMetricNamesSortsThemByClassThenScopeThenName() throws Exception {
        final MetricName one = new MetricName(Object.class, "one");
        final MetricName two = new MetricName(Object.class, "two");
        final MetricName three = new MetricName(String.class, "three");

        final Counter mOne = registry.counter()
                                     .forClass(Object.class)
                                     .named("one")
                                     .build();
        final Counter mTwo = registry.counter()
                                     .forClass(Object.class)
                                     .named("two")
                                     .build();
        final Counter mThree = registry.counter()
                                       .forClass(String.class)
                                       .named("three")
                                       .build();

        final SortedMap<String, SortedMap<MetricName, Metric>> sortedMetrics = new TreeMap<String, SortedMap<MetricName, Metric>>();
        final TreeMap<MetricName, Metric> objectMetrics = new TreeMap<MetricName, Metric>();
        objectMetrics.put(one, mOne);
        objectMetrics.put(two, mTwo);
        sortedMetrics.put(Object.class.getCanonicalName(), objectMetrics);

        final TreeMap<MetricName, Metric> stringMetrics = new TreeMap<MetricName, Metric>();
        stringMetrics.put(three, mThree);
        sortedMetrics.put(String.class.getCanonicalName(), stringMetrics);

        assertThat(registry.getGroupedMetrics(),
                   is(sortedMetrics));
    }

    @Test
    public void listenersRegisterNewMetrics() throws Exception {
        final MetricsRegistryListener listener = mock(MetricsRegistryListener.class);
        registry.addListener(listener);

        final Gauge<?> gauge = mock(Gauge.class);
        registry.register(new MetricName(MetricsRegistryTest.class, "gauge"), gauge);

        final Counter counter = registry.counter()
                                        .forClass(MetricsRegistryTest.class)
                                        .named("counter")
                                        .build();
        final Histogram histogram = registry.histogram()
                                            .forClass(MetricsRegistryTest.class)
                                            .named("histogram")
                                            .buildBiased();
        final Meter meter = registry.meter()
                                    .forClass(MetricsRegistryTest.class)
                                    .named("meter")
                                    .measuring("things")
                                    .build();
        final Timer timer = registry.timer()
                                    .forClass(MetricsRegistryTest.class)
                                    .named("timer")
                                    .build();

        verify(listener).onMetricAdded(new MetricName(MetricsRegistryTest.class, "gauge"), gauge);

        verify(listener).onMetricAdded(new MetricName(MetricsRegistryTest.class, "counter"), counter);

        verify(listener).onMetricAdded(new MetricName(MetricsRegistryTest.class, "histogram"), histogram);

        verify(listener).onMetricAdded(new MetricName(MetricsRegistryTest.class, "meter"), meter);

        verify(listener).onMetricAdded(new MetricName(MetricsRegistryTest.class, "timer"), timer);
    }

    @Test
    public void removedListenersDoNotReceiveEvents() throws Exception {
        final MetricsRegistryListener listener = mock(MetricsRegistryListener.class);
        registry.addListener(listener);

        final Counter counter1 = registry.counter()
                                         .forClass(MetricsRegistryTest.class)
                                         .named("counter1")
                                         .build();

        registry.removeListener(listener);

        final Counter counter2 = registry.counter()
                                        .forClass(MetricsRegistryTest.class)
                                        .named("counter2")
                                        .build();

        verify(listener).onMetricAdded(new MetricName(MetricsRegistryTest.class, "counter1"), counter1);

        verify(listener, never()).onMetricAdded(new MetricName(MetricsRegistryTest.class, "counter2"), counter2);
    }

    @Test
    public void metricsCanBeRemoved() throws Exception {
        final MetricsRegistryListener listener = mock(MetricsRegistryListener.class);
        registry.addListener(listener);

        final MetricName name = new MetricName(MetricsRegistryTest.class, "counter1");

        final Counter counter1 = registry.counter()
                                         .forClass(MetricsRegistryTest.class)
                                         .named("counter1")
                                         .build();
        registry.unregister(new MetricName(MetricsRegistryTest.class, "counter1"));

        final InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onMetricAdded(name, counter1);
        inOrder.verify(listener).onMetricRemoved(name);
    }
}
