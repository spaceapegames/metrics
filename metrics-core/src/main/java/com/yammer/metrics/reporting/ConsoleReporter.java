package com.yammer.metrics.reporting;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.stats.Snapshot;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * A simple reporters which prints out application metrics to a {@link PrintStream} periodically.
 */
public class ConsoleReporter extends AbstractPollingReporter implements
                                                             MetricProcessor<PrintStream> {
    private static final int CONSOLE_WIDTH = 80;

    private final PrintStream out;
    private final MetricPredicate predicate;
    private final Clock clock;
    private final TimeUnit durationUnit;
    private final TimeZone timeZone;
    private final Locale locale;

    public ConsoleReporter() {
        this(Metrics.defaultRegistry(),
             System.out,
             MetricPredicate.ALL,
             Clock.defaultClock(),
             TimeUnit.MILLISECONDS,
             TimeZone.getDefault(),
             Locale.getDefault());
    }

    /**
     * Creates a new {@link ConsoleReporter} for a given metrics registry.
     *
     * @param metricRegistry the metrics registry
     * @param out             the {@link PrintStream} to which output will be written
     * @param predicate       the {@link MetricPredicate} used to determine whether a metric will be
     *                        output
     * @param clock           the {@link com.yammer.metrics.core.Clock} used to print time
     * @param timeZone        the {@link TimeZone} used to print time
     * @param durationUnit    the {@link TimeUnit} in which to print durations
     * @param locale          the {@link Locale} used to print values
     */
    public ConsoleReporter(MetricRegistry metricRegistry,
                           PrintStream out,
                           MetricPredicate predicate,
                           Clock clock,
                           TimeUnit durationUnit,
                           TimeZone timeZone, Locale locale) {
        super(metricRegistry, "console-reporter");
        this.out = out;
        this.predicate = predicate;
        this.clock = clock;
        this.durationUnit = durationUnit;
        this.timeZone = timeZone;
        this.locale = locale;
    }

    @Override
    public void run() {
        try {
            final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                                     DateFormat.MEDIUM,
                                                                     locale);
            final MetricDispatcher dispatcher = new MetricDispatcher();
            format.setTimeZone(timeZone);
            final String dateTime = format.format(new Date(clock.getTime()));
            out.print(dateTime);
            out.print(' ');
            for (int i = 0; i < (CONSOLE_WIDTH - dateTime.length() - 1); i++) {
                out.print('=');
            }
            out.println();
            for (Entry<String, Metric> entry : getMetricRegistry().filter(predicate)) {
                out.print(entry.getKey());
                out.println(':');
                dispatcher.dispatch(entry.getValue(), entry.getKey(), this, out);
                out.println();
            }
            out.println();
            out.flush();
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

    @Override
    public void processGauge(String name, Gauge<?> gauge, PrintStream stream) {
        stream.printf(locale, "    value = %s\n", gauge.getValue());
    }

    @Override
    public void processCounter(String name, Counter counter, PrintStream stream) {
        stream.printf(locale, "    count = %d\n", counter.getCount());
    }

    @Override
    public void processMeter(String name, Metered meter, PrintStream stream) {
        stream.printf(locale, "             count = %d\n", meter.getCount());
        stream.printf(locale, "         mean rate = %2.2f events/s\n", meter.getMeanRate());
        stream.printf(locale, "     1-minute rate = %2.2f events/s\n", meter.getOneMinuteRate());
        stream.printf(locale, "     5-minute rate = %2.2f events/s\n", meter.getFiveMinuteRate());
        stream.printf(locale, "    15-minute rate = %2.2f events/s\n", meter.getFifteenMinuteRate());
    }

    @Override
    public void processHistogram(String name, Histogram histogram, PrintStream stream) {
        final Snapshot snapshot = histogram.getSnapshot();
        stream.printf(locale, "               min = %d\n", histogram.getMin());
        stream.printf(locale, "               max = %d\n", histogram.getMax());
        stream.printf(locale, "              mean = %d\n", histogram.getMean());
        stream.printf(locale, "            stddev = %2.2f\n", histogram.getStdDev());
        stream.printf(locale, "            median = %d\n", snapshot.getMedian());
        stream.printf(locale, "              75%% <= %d\n", snapshot.get75thPercentile());
        stream.printf(locale, "              95%% <= %d\n", snapshot.get95thPercentile());
        stream.printf(locale, "              98%% <= %d\n", snapshot.get98thPercentile());
        stream.printf(locale, "              99%% <= %d\n", snapshot.get99thPercentile());
        stream.printf(locale, "            99.9%% <= %d\n", snapshot.get999thPercentile());
    }

    @Override
    public void processTimer(String name, Timer timer, PrintStream stream) {
        processMeter(name, timer, stream);
        final Snapshot snapshot = timer.getSnapshot();
        stream.printf(locale, "               min = %2.2fms\n", convertFromNS(timer.getMin()));
        stream.printf(locale, "               max = %2.2fms\n", convertFromNS(timer.getMax()));
        stream.printf(locale, "              mean = %2.2fms\n", convertFromNS(timer.getMean()));
        stream.printf(locale, "            stddev = %2.2fms\n", convertFromNS(timer.getStdDev()));
        stream.printf(locale, "            median = %2.2fms\n", convertFromNS(snapshot.getMedian()));
        stream.printf(locale, "              75%% <= %2.2fms\n", convertFromNS(snapshot.get75thPercentile()));
        stream.printf(locale, "              95%% <= %2.2fms\n", convertFromNS(snapshot.get95thPercentile()));
        stream.printf(locale, "              98%% <= %2.2fms\n", convertFromNS(snapshot.get98thPercentile()));
        stream.printf(locale, "              99%% <= %2.2fms\n", convertFromNS(snapshot.get99thPercentile()));
        stream.printf(locale, "            99.9%% <= %2.2fms\n", convertFromNS(snapshot.get999thPercentile()));
    }

    private String abbrev(TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "us";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "m";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new IllegalArgumentException("Unrecognized TimeUnit: " + unit);
        }
    }

    private double convertFromNS(long ns) {
        return ns / (double) durationUnit.toNanos(1);
    }

    private double convertFromNS(double ns) {
        return ns / (double) durationUnit.toNanos(1);
    }
}
