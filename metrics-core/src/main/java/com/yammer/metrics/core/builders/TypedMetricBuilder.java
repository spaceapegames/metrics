package com.yammer.metrics.core.builders;

import com.yammer.metrics.core.Metric;

public interface TypedMetricBuilder<T extends Metric> {
    BoundMetricBuilder<T> domain(String domain);
}
