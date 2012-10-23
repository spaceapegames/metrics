package com.yammer.metrics.jetty;

import com.yammer.metrics.core.MetricGroup;
import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.util.RatioGauge;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;

public class InstrumentedQueuedThreadPool extends QueuedThreadPool {
    public InstrumentedQueuedThreadPool() {
        this(Metrics.defaultRegistry());
    }

    public InstrumentedQueuedThreadPool(MetricRegistry registry) {
        super();
        final MetricGroup metrics = registry.group(QueuedThreadPool.class);

        metrics.gauge("percent-idle").build(new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return new Ratio(getIdleThreads(), getThreads());
            }
        });

        metrics.gauge("active-threads").build(new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return getThreads();
            }
        });

        metrics.gauge("idle-threads").build(new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return getIdleThreads();
            }
        });
    }
}
