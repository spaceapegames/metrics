package com.yammer.metrics.graphite;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.reporting.tests.AbstractPollingReporterTest;

import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphiteReporterTest extends AbstractPollingReporterTest {
    @Override
    protected AbstractPollingReporter createReporter(MetricRegistry registry, OutputStream out, Clock clock) throws Exception {
        final Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(out);

        final SocketProvider provider = mock(SocketProvider.class);
        when(provider.get()).thenReturn(socket);

        final GraphiteReporter reporter = new GraphiteReporter(registry,
                                                               "prefix",
                                                               MetricPredicate.ALL,
                                                               provider,
                                                               clock,
                                                               TimeUnit.MILLISECONDS,
                                                               VirtualMachineMetrics.getInstance(),
                                                               "graphite-reporter");
        reporter.printVMMetrics = false;
        return reporter;
    }

    @Override
    public String[] expectedGaugeResult(String value) {
        return new String[]{String.format("prefix.java.lang.Object.metric.value %s 5", value)};
    }

    @Override
    public String[] expectedTimerResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.count 1 5",
                "prefix.java.lang.Object.metric.meanRate 2.00 5",
                "prefix.java.lang.Object.metric.1MinuteRate 1.00 5",
                "prefix.java.lang.Object.metric.5MinuteRate 5.00 5",
                "prefix.java.lang.Object.metric.15MinuteRate 15.00 5",
                "prefix.java.lang.Object.metric.min 0.00 5",
                "prefix.java.lang.Object.metric.max 99.00 5",
                "prefix.java.lang.Object.metric.mean 49.50 5",
                "prefix.java.lang.Object.metric.stddev 29.01 5",
                "prefix.java.lang.Object.metric.median 49.50 5",
                "prefix.java.lang.Object.metric.75percentile 74.75 5",
                "prefix.java.lang.Object.metric.95percentile 94.95 5",
                "prefix.java.lang.Object.metric.98percentile 97.98 5",
                "prefix.java.lang.Object.metric.99percentile 98.99 5",
                "prefix.java.lang.Object.metric.999percentile 99.00 5"
        };
    }

    @Override
    public String[] expectedMeterResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.count 1 5",
                "prefix.java.lang.Object.metric.meanRate 2.00 5",
                "prefix.java.lang.Object.metric.1MinuteRate 1.00 5",
                "prefix.java.lang.Object.metric.5MinuteRate 5.00 5",
                "prefix.java.lang.Object.metric.15MinuteRate 15.00 5",
        };
    }

    @Override
    public String[] expectedHistogramResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.min 0 5",
                "prefix.java.lang.Object.metric.max 99 5",
                "prefix.java.lang.Object.metric.mean 49 5",
                "prefix.java.lang.Object.metric.stddev 29.01 5",
                "prefix.java.lang.Object.metric.median 49 5",
                "prefix.java.lang.Object.metric.75percentile 74 5",
                "prefix.java.lang.Object.metric.95percentile 94 5",
                "prefix.java.lang.Object.metric.98percentile 97 5",
                "prefix.java.lang.Object.metric.99percentile 98 5",
                "prefix.java.lang.Object.metric.999percentile 99 5"
        };
    }

    @Override
    public String[] expectedCounterResult(long count) {
        return new String[]{
                String.format("prefix.java.lang.Object.metric.count %d 5", count)
        };
    }
}
