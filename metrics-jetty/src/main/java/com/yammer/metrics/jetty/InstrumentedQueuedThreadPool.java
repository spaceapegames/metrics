package com.yammer.metrics.jetty;

import com.yammer.metrics.util.RatioGauge;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricRegistry;

public class InstrumentedQueuedThreadPool extends QueuedThreadPool {
    public InstrumentedQueuedThreadPool() {
        this(Metrics.defaultRegistry());
    }

    public InstrumentedQueuedThreadPool(MetricRegistry registry) {
        super();
        registry.add(Metrics.name(QueuedThreadPool.class, "percent-idle"),
                     new RatioGauge() {
                         @Override
                         protected Ratio getRatio() {
                             return Ratio.of(getIdleThreads(), getThreads());
                         }
                     });
        registry.add(Metrics.name(QueuedThreadPool.class, "active-threads"),
                     new Gauge<Integer>() {
                         @Override
                         public Integer getValue() {
                             return getThreads();
                         }
                     });
        registry.add(Metrics.name(QueuedThreadPool.class, "idle-threads"),
                     new Gauge<Integer>() {
                         @Override
                         public Integer getValue() {
                             return getIdleThreads();
                         }
                     });
    }
}
