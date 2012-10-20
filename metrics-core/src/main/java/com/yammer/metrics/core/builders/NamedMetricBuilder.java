package com.yammer.metrics.core.builders;

import com.yammer.metrics.core.Metric;

public interface NamedMetricBuilder<T extends Metric> {
    ScopedMetricBuilder<T> scopedTo(String scope);
}
