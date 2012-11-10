package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.reporting.CsvReporter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

public class CsvReporterTest extends AbstractPollingReporterTest {

    @Override
    protected AbstractPollingReporter createReporter(MetricRegistry registry, final OutputStream out, Clock clock) throws Exception {
        return new CsvReporter(registry, MetricPredicate.ALL, new File("/tmp"), TimeUnit.MILLISECONDS, clock) {
            @Override
            protected PrintStream createStreamForMetric(String metricName) throws IOException {
                return new PrintStream(out);
            }
        };
    }

    @Override
    public String[] expectedCounterResult(long count) {
        return new String[]{"timestamp,count", String.format("5678,%s\n", count)};
    }

    @Override
    public String[] expectedHistogramResult() {
        return new String[]{"timestamp,min,max,mean,median,stddev,95%,99%,99.9%",
                            "5678,0,99,49,49,29.011491975882016,94,98,99\n"};
    }

    @Override
    public String[] expectedMeterResult() {
        return new String[]{"timestamp,count,1 min rate,mean rate,5 min rate,15 min rate",
                            "5678,1,1.0,2.0,5.0,15.0\n"};
    }

    @Override
    public String[] expectedTimerResult() {
        return new String[]{"timestamp,min,max,mean,median,stddev,95%,99%,99.9%,count,1 min rate,mean rate,5 min rate,15 min rate",
                            "5678,0.0,99.0,49.5,49.5,29.011491975882016,94.949999,98.99,99.0,1,1.0,2.0,5.0,15.0\n"};
    }

    @Override
    public String[] expectedGaugeResult(String value) {
        return new String[]{"timestamp,value", String.format("5678,%s\n", value)};
    }
}
