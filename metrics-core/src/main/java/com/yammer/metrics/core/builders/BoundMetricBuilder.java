package com.yammer.metrics.core.builders;

import com.yammer.metrics.core.Metric;

public interface BoundMetricBuilder<T extends Metric> {
    NamedMetricBuilder<T> named(String name);
}
