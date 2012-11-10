package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.reporting.ConsoleReporter;
import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

public class ConsoleReporterTest extends AbstractPollingReporterTest {
    @Override
    protected AbstractPollingReporter createReporter(MetricRegistry registry, OutputStream out, Clock clock) {
        return new ConsoleReporter(registry,
                                   new PrintStream(out),
                                   MetricPredicate.ALL,
                                   clock,
                                   TimeUnit.MILLISECONDS,
                                   TimeZone.getTimeZone("UTC"),
                                   Locale.US);
    }

    @Override
    public String[] expectedCounterResult(long count) {
        return new String[]{
                "1/1/70 12:00:05 AM =============================================================",
                "java.lang.Object.metric:",
                "count = " + count
        };
    }

    @Override
    public String[] expectedHistogramResult() {
        return new String[]{
                "1/1/70 12:00:05 AM =============================================================",
                "java.lang.Object.metric:",
                "min = 0",
                "max = 99",
                "mean = 49",
                "stddev = 29.01",
                "median = 49",
                "75% <= 74",
                "95% <= 94",
                "98% <= 97",
                "99% <= 98",
                "99.9% <= 99"
        };
    }

    @Override
    public String[] expectedMeterResult() {
        return new String[]{
                "1/1/70 12:00:05 AM =============================================================",
                "java.lang.Object.metric:",
                "count = 1",
                "mean rate = 2.00 events/s",
                "1-minute rate = 1.00 events/s",
                "5-minute rate = 5.00 events/s",
                "15-minute rate = 15.00 events/s"
        };
    }

    @Override
    public String[] expectedTimerResult() {
        return new String[]{
                "1/1/70 12:00:05 AM =============================================================",
                "java.lang.Object.metric:", "" +
                "count = 1",
                "mean rate = 2.00 events/s",
                "1-minute rate = 1.00 events/s",
                "5-minute rate = 5.00 events/s",
                "15-minute rate = 15.00 events/s",
                "min = 0.00ms",
                "max = 99.00ms",
                "mean = 49.50ms",
                "stddev = 29.01ms",
                "median = 49.50ms",
                "75% <= 74.75ms",
                "95% <= 94.95ms",
                "98% <= 97.98ms",
                "99% <= 98.99ms",
                "99.9% <= 99.00ms"
        };
    }

    @Override
    public String[] expectedGaugeResult(String value) {
        return new String[]{
                "1/1/70 12:00:05 AM =============================================================",
                "java.lang.Object.metric:",
                String.format("value = %s", value)
        };
    }

    @Test
    public void givenShutdownReporterWhenCreatingNewReporterExpectSuccess() {
        try {
            final ConsoleReporter reporter1 = new ConsoleReporter();
            reporter1.start(1, TimeUnit.SECONDS);
            reporter1.shutdown();
            final ConsoleReporter reporter2 = new ConsoleReporter();
            reporter2.start(1, TimeUnit.SECONDS);
            reporter2.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            fail("should be able to start and shutdown reporters");
        }
    }
}
