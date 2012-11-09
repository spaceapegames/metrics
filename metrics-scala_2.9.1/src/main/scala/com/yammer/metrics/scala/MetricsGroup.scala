package com.yammer.metrics.scala

import com.yammer.metrics.Metrics
import com.yammer.metrics.core.{MetricRegistry, Gauge}

/**
 * A helper class for creating and registering metrics.
 */
class MetricsGroup(val klass: Class[_], val registry: MetricRegistry = Metrics.defaultRegistry()) {

  /**
   * Registers a new gauge metric.
   *
   * @param name  the name of the gauge
   */
  def gauge[A](name: String*)(f: => A) = {
    registry.add(Metrics.name(klass, name:_*), new Gauge[A] {
      def getValue = f
    })
  }

  /**
   * Creates a new counter metric.
   *
   * @param name  the name of the counter
   */
  def counter(name: String*) =
    new Counter(registry.counter(Metrics.name(klass, name:_*)))

  /**
   * Creates a new histogram metrics.
   *
   * @param name   the name of the histogram
   */
  def histogram(name: String*) =
    new Histogram(registry.histogram(Metrics.name(klass, name:_*)))

  /**
   * Creates a new meter metric.
   *
   * @param name the name of the meter
   */
  def meter(name: String*) =
    new Meter(registry.meter(Metrics.name(klass, name:_*)))

  /**
   * Creates a new timer metric.
   *
   * @param name the name of the timer
   */
  def timer(name: String*) =
    new Timer(registry.timer(Metrics.name(klass, name:_*)))
}

