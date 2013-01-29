package com.yammer.metrics.scala.tests

import org.junit.Test
import com.yammer.metrics.scala.Counter
import org.scalatest.matchers.ShouldMatchers
import com.simple.simplespec.Mocks;

class CounterSpec extends ShouldMatchers with Mocks {
    val metric = mock[com.yammer.metrics.core.Counter]
    val counter = new Counter(metric)

    @Test def `A counter increments the underlying metric by an arbitrary amount` {
      counter += 12

      verify.one(metric).inc(12)
    }

    @Test def `A counter decrements the underlying metric by an arbitrary amount` {
      counter -= 12

      verify.one(metric).dec(12)
    }
}

