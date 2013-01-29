package com.yammer.metrics.scala.tests

import org.junit.Test
import com.yammer.metrics.scala.Meter
import org.scalatest.matchers.ShouldMatchers
import com.simple.simplespec.Mocks

class MeterSpec extends ShouldMatchers with Mocks{
    val metric = mock[com.yammer.metrics.core.Meter]
    val meter = new Meter(metric)

    @Test def `A meter marks the underlying metric` {
      meter.mark()

      verify.one(metric).mark()
    }

    @Test def `A meter marks the underlying metric by an arbitrary amount` {
      meter.mark(12)

      verify.one(metric).mark(12)
    }
}

