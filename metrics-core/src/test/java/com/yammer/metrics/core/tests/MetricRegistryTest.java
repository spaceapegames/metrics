package com.yammer.metrics.core.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

public class MetricRegistryTest {
    private final MetricRegistry registry = new MetricRegistry();

    @Test
    public void listenersRegisterNewMetrics() throws Exception {
        final MetricRegistryListener listener = mock(MetricRegistryListener.class);
        registry.addListener(listener);

        final Gauge<?> gauge = registry.add(Metrics.name(MetricRegistryTest.class, "gauge"),
                                            mock(Gauge.class));
        final Counter counter = registry.add(Metrics.name(MetricRegistryTest.class, "counter"),
                                             new Counter());
        final Histogram histogram = registry.add(Metrics.name(MetricRegistryTest.class, "histogram"),
                                                 new Histogram(Histogram.SampleType.UNIFORM));
        final Meter meter = registry.add(Metrics.name(MetricRegistryTest.class, "meter"),
                                         new Meter("things"));
        final Timer timer = registry.add(Metrics.name(MetricRegistryTest.class, "timer"),
                                         new Timer());

        verify(listener).onMetricAdded(Metrics.name(MetricRegistryTest.class, "gauge"),
                                       gauge);

        verify(listener).onMetricAdded(Metrics.name(MetricRegistryTest.class, "counter"),
                                       counter);

        verify(listener).onMetricAdded(Metrics.name(MetricRegistryTest.class, "histogram"),
                                       histogram);

        verify(listener).onMetricAdded(Metrics.name(MetricRegistryTest.class, "meter"),
                                       meter);

        verify(listener).onMetricAdded(Metrics.name(MetricRegistryTest.class, "timer"),
                                       timer);
    }

    @Test
    public void removedListenersDoNotReceiveEvents() throws Exception {
        final MetricRegistryListener listener = mock(MetricRegistryListener.class);
        registry.addListener(listener);

        final Counter counter1 = registry.add(Metrics.name(MetricRegistryTest.class,
                                                           "counter1"),
                                              new Counter());

        registry.removeListener(listener);

        final Counter counter2 = registry.add(Metrics.name(MetricRegistryTest.class,
                                                           "counter2"),
                                              new Counter());

        verify(listener).onMetricAdded(Metrics.name(MetricRegistryTest.class, "counter1"),
                                       counter1);

        verify(listener, never()).onMetricAdded(Metrics.name(MetricRegistryTest.class,
                                                             "counter2"),
                                                counter2);
    }

    @Test
    public void metricsCanBeRemoved() throws Exception {
        final MetricRegistryListener listener = mock(MetricRegistryListener.class);
        registry.addListener(listener);

        final String name = Metrics.name(MetricRegistryTest.class, "counter1");

        final Counter counter1 = registry.add(Metrics.name(MetricRegistryTest.class,
                                                           "counter1"),
                                              new Counter());
        registry.remove(Metrics.name(MetricRegistryTest.class, "counter1"));

        final InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onMetricAdded(name, counter1);
        inOrder.verify(listener).onMetricRemoved(name);
    }
}
