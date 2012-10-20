package com.yammer.metrics.core.builders;

import com.yammer.metrics.core.Metric;

public interface MetricBuilder<T extends Metric> {
    // handles both domain
    BoundMetricBuilder<T> forClass(Class<?> klass);

    // just the domain
    DomainMetricBuilder<T> domain(String domain);

    // just the type
    TypedMetricBuilder<T> type(String type);
}
