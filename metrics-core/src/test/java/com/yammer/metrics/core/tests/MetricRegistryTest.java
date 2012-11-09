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

        @SuppressWarnings("unchecked")
        final Gauge<?> gauge = registry.gauge(Metrics.name(MetricRegistryTest.class, "gauge"),
                                              mock(Gauge.class));
        final Counter counter = registry.counter(Metrics.name(MetricRegistryTest.class, "counter"));
        final Histogram histogram = registry.histogram(Metrics.name(MetricRegistryTest.class,
                                                                    "histogram"));
        final Meter meter = registry.meter(Metrics.name(MetricRegistryTest.class, "meter"));
        final Timer timer = registry.timer(Metrics.name(MetricRegistryTest.class, "timer"));

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

        final Counter counter1 = registry.counter(Metrics.name(MetricRegistryTest.class,
                                                               "counter1"));

        registry.removeListener(listener);

        final Counter counter2 = registry.counter(Metrics.name(MetricRegistryTest.class,
                                                               "counter2"));

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

        final Counter counter1 = registry.counter(Metrics.name(MetricRegistryTest.class,
                                                               "counter1"));
        registry.remove(Metrics.name(MetricRegistryTest.class, "counter1"));

        final InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onMetricAdded(name, counter1);
        inOrder.verify(listener).onMetricRemoved(name);
    }
}
