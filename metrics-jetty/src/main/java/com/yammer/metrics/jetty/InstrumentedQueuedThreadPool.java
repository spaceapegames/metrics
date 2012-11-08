package com.yammer.metrics.jetty;

import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.util.RatioGauge;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;

public class InstrumentedQueuedThreadPool extends QueuedThreadPool {
    public InstrumentedQueuedThreadPool() {
        this(Metrics.defaultRegistry());
    }

    public InstrumentedQueuedThreadPool(MetricsRegistry registry) {
        super();
        registry.add(MetricName.name(QueuedThreadPool.class, "percent-idle"),
                     new RatioGauge() {
                         @Override
                         protected double getNumerator() {
                             return getIdleThreads();
                         }

                         @Override
                         protected double getDenominator() {
                             return getThreads();
                         }
                     });
        registry.add(MetricName.name(QueuedThreadPool.class, "active-threads"),
                     new Gauge<Integer>() {
                         @Override
                         public Integer getValue() {
                             return getThreads();
                         }
                     });
        registry.add(MetricName.name(QueuedThreadPool.class, "idle-threads"),
                     new Gauge<Integer>() {
                         @Override
                         public Integer getValue() {
                             return getIdleThreads();
                         }
                     });
    }
}
