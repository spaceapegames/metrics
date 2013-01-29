package com.yammer.metrics.scala.tests

import org.junit.Test
import com.yammer.metrics.Metrics
import com.yammer.metrics.scala.Timer
import org.scalatest.matchers.ShouldMatchers

class TimerSpec extends ShouldMatchers {
    val metric = Metrics.defaultRegistry().newTimer(classOf[TimerSpec], "timer")
    val timer = new Timer(metric)

    @Test def `A timer updates the underlying metric` {
      timer.time { Thread.sleep(100); 10 } should equal (10)

      metric.getMin should be > (100.0 - 10)
      metric.getMin should be < (100.0 + 10)
    }
}

