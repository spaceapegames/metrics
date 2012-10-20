package com.yammer.metrics.jetty;

import com.yammer.metrics.core.MetricsGroup;
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
        final MetricsGroup metrics = registry.group(QueuedThreadPool.class);

        metrics.gauge("percent-idle").build(new RatioGauge() {
            @Override
            protected double getNumerator() {
                return getIdleThreads();
            }

            @Override
            protected double getDenominator() {
                return getThreads();
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
