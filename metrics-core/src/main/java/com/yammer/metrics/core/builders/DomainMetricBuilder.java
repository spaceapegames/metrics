package com.yammer.metrics.core.builders;

import com.yammer.metrics.core.Metric;

public interface DomainMetricBuilder<T extends Metric> {
    BoundMetricBuilder<T> type(String type);
}
