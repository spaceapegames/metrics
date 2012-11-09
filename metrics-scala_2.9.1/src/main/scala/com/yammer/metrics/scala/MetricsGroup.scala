package com.yammer.metrics.scala

import java.util.concurrent.TimeUnit
import com.yammer.metrics.Metrics
import com.yammer.metrics.core.{MetricRegistry, Gauge}
import com.yammer.metrics.core.Histogram.SampleType

/**
 * A helper class for creating and registering metrics.
 */
class MetricsGroup(val klass: Class[_], val metricsRegistry: MetricRegistry = Metrics.defaultRegistry()) {

  /**
   * Registers a new gauge metric.
   *
   * @param name  the name of the gauge
   * @param scope the scope of the gauge
   * @param registry the registry for the gauge
   */
  def gauge[A](name: String, scope: String = null, registry: MetricRegistry = metricsRegistry)(f: => A) = {
    registry.add(Metrics.name(klass, name, scope), new Gauge[A] {
      def getValue = f
    })
  }

  /**
   * Creates a new counter metric.
   *
   * @param name  the name of the counter
   * @param scope the scope of the gauge
   * @param registry the registry for the gauge
   */
  def counter(name: String, scope: String = null, registry: MetricRegistry = metricsRegistry) =
    new Counter(registry.counter(Metrics.name(klass, name, scope)))

  /**
   * Creates a new histogram metrics.
   *
   * @param name   the name of the histogram
   * @param scope  the scope of the histogram
   * @param registry the registry for the gauge
   */
  def histogram(name: String,
                scope: String = null,
                registry: MetricRegistry = metricsRegistry) =
    new Histogram(registry.histogram(Metrics.name(klass, name, scope)))

  /**
   * Creates a new meter metric.
   *
   * @param name the name of the meter
   * @param scope the scope of the meter
   * @param registry the registry for the gauge
   */
  def meter(name: String,
            scope: String = null,
            registry: MetricRegistry = metricsRegistry) =
    new Meter(registry.meter(Metrics.name(klass, name, scope)))

  /**
   * Creates a new timer metric.
   *
   * @param name the name of the timer
   * @param scope the scope of the timer
   * @param registry the registry for the gauge
   */
  def timer(name: String,
            scope: String = null,
            registry: MetricRegistry = metricsRegistry) =
    new Timer(registry.timer(Metrics.name(klass, name, scope)))
}

