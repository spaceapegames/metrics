package com.yammer.metrics.reporting;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.stats.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which periodically appends data from each metric to a metric-specific CSV file in
 * an output directory.
 */
public class CsvReporter extends AbstractPollingReporter implements
                                                         MetricProcessor<CsvReporter.Context> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvReporter.class);

    /**
     * Enables the CSV reporter for the default metrics registry, and causes it to write to files in
     * {@code outputDir} with the specified period.
     *
     * @param outputDir    the directory in which {@code .csv} files will be created
     * @param period       the period between successive outputs
     * @param unit         the time unit of {@code period}
     */
    public static void enable(File outputDir, long period, TimeUnit unit) {
        enable(Metrics.defaultRegistry(), outputDir, period, unit);
    }

    /**
     * Enables the CSV reporter for the given metrics registry, and causes it to write to files in
     * {@code outputDir} with the specified period.
     *
     * @param metricRegistry the metrics registry
     * @param outputDir       the directory in which {@code .csv} files will be created
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     */
    public static void enable(MetricRegistry metricRegistry, File outputDir, long period, TimeUnit unit) {
        final CsvReporter reporter = new CsvReporter(metricRegistry, outputDir);
        reporter.start(period, unit);
    }

    /**
     * The context used to output metrics.
     */
    public interface Context {
        /**
         * Returns an open {@link PrintStream} for the metric with {@code header} already written
         * to it.
         *
         * @param header    the CSV header
         * @return an open {@link PrintStream}
         * @throws IOException if there is an error opening the stream or writing to it
         */
        PrintStream getStream(String header) throws IOException;
    }

    private final MetricPredicate predicate;
    private final File outputDir;
    private final Map<String, PrintStream> streamMap;
    private final Clock clock;
    private long startTime;

    /**
     * Creates a new {@link CsvReporter} which will write all metrics from the given
     * {@link com.yammer.metrics.core.MetricRegistry} to CSV files in the given output directory.
     *
     * @param outputDir          the directory to which files will be written
     * @param metricRegistry    the {@link com.yammer.metrics.core.MetricRegistry} containing the metrics this reporter
     *                           will report
     */
    public CsvReporter(MetricRegistry metricRegistry, File outputDir) {
        this(metricRegistry, MetricPredicate.ALL, outputDir);
    }

    /**
     * Creates a new {@link CsvReporter} which will write metrics from the given
     * {@link com.yammer.metrics.core.MetricRegistry} which match the given {@link MetricPredicate} to CSV files in the
     * given output directory.
     *
     * @param metricRegistry    the {@link com.yammer.metrics.core.MetricRegistry} containing the metrics this reporter
     *                           will report
     * @param predicate          the {@link MetricPredicate} which metrics are required to match
     *                           before being written to files
     * @param outputDir          the directory to which files will be written
     */
    public CsvReporter(MetricRegistry metricRegistry,
                       MetricPredicate predicate,
                       File outputDir) {
        this(metricRegistry, predicate, outputDir, Clock.defaultClock());
    }

    /**
     * Creates a new {@link CsvReporter} which will write metrics from the given
     * {@link com.yammer.metrics.core.MetricRegistry} which match the given {@link MetricPredicate} to CSV files in the
     * given output directory.
     *
     * @param metricRegistry    the {@link com.yammer.metrics.core.MetricRegistry} containing the metrics this reporter
     *                           will report
     * @param predicate          the {@link MetricPredicate} which metrics are required to match
     *                           before being written to files
     * @param outputDir          the directory to which files will be written
     * @param clock              the clock used to measure time
     */
    public CsvReporter(MetricRegistry metricRegistry,
                       MetricPredicate predicate,
                       File outputDir,
                       Clock clock) {
        super(metricRegistry, "csv-reporter");
        if (outputDir.exists() && !outputDir.isDirectory()) {
            throw new IllegalArgumentException(outputDir + " is not a directory");
        }
        this.outputDir = outputDir;
        this.predicate = predicate;
        this.streamMap = new HashMap<String, PrintStream>();
        this.startTime = 0L;
        this.clock = clock;
    }

    /**
     * Returns an opened {@link PrintStream} for the given {@link MetricName} which outputs data
     * to a metric-specific {@code .csv} file in the output directory.
     *
     * @param metricName    the name of the metric
     * @return an opened {@link PrintStream} specific to {@code metricName}
     * @throws IOException if there is an error opening the stream
     */
    protected PrintStream createStreamForMetric(String metricName) throws IOException {
        final File newFile = new File(outputDir, metricName + ".csv");
        if (newFile.createNewFile()) {
            return new PrintStream(new FileOutputStream(newFile));
        }
        throw new IOException("Unable to create " + newFile);
    }

    @Override
    public void run() {
        final long time = TimeUnit.MILLISECONDS.toSeconds(clock.getTime() - startTime);
        final MetricDispatcher dispatcher = new MetricDispatcher();
        try {
            for (Entry<String, Metric> entry : getMetricRegistry().filter(predicate)) {
                final String name = entry.getKey();
                final Metric metric = entry.getValue();
                if (metric != null) {
                    final Context context = new Context() {
                        @Override
                        public PrintStream getStream(String header) throws IOException {
                            final PrintStream stream = getPrintStream(name, header);
                            stream.print(time);
                            stream.print(',');
                            return stream;
                        }

                    };
                    dispatcher.dispatch(metric, name, this, context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processMeter(String name, Metered meter, Context context) throws IOException {
        final PrintStream stream = context.getStream(
                "# time,count,1 min rate,mean rate,5 min rate,15 min rate");
        stream.append(new StringBuilder()
                              .append(meter.getCount()).append(',')
                              .append(meter.getOneMinuteRate()).append(',')
                              .append(meter.getMeanRate()).append(',')
                              .append(meter.getFiveMinuteRate()).append(',')
                              .append(meter.getFifteenMinuteRate()).toString())
              .println();
        stream.flush();
    }

    @Override
    public void processCounter(String name, Counter counter, Context context) throws IOException {
        final PrintStream stream = context.getStream("# time,count");
        stream.println(counter.getCount());
        stream.flush();
    }

    @Override
    public void processHistogram(String name, Histogram histogram, Context context) throws IOException {
        final PrintStream stream = context.getStream("# time,min,max,mean,median,stddev,95%,99%,99.9%");
        final Snapshot snapshot = histogram.getSnapshot();
        stream.append(new StringBuilder()
                              .append(histogram.getMin()).append(',')
                              .append(histogram.getMax()).append(',')
                              .append(histogram.getMean()).append(',')
                              .append(snapshot.getMedian()).append(',')
                              .append(histogram.getStdDev()).append(',')
                              .append(snapshot.get95thPercentile()).append(',')
                              .append(snapshot.get99thPercentile()).append(',')
                              .append(snapshot.get999thPercentile()).toString())
                .println();
        stream.println();
        stream.flush();
    }

    @Override
    public void processTimer(String name, Timer timer, Context context) throws IOException {
        final PrintStream stream = context.getStream("# time,min,max,mean,median,stddev,95%,99%,99.9%");
        final Snapshot snapshot = timer.getSnapshot();
        stream.append(new StringBuilder()
                              .append(timer.getMin()).append(',')
                              .append(timer.getMax()).append(',')
                              .append(timer.getMean()).append(',')
                              .append(snapshot.getMedian()).append(',')
                              .append(timer.getStdDev()).append(',')
                              .append(snapshot.get95thPercentile()).append(',')
                              .append(snapshot.get99thPercentile()).append(',')
                              .append(snapshot.get999thPercentile()).toString())
                .println();
        stream.flush();
    }

    @Override
    public void processGauge(String name, Gauge<?> gauge, Context context) throws IOException {
        final PrintStream stream = context.getStream("# time,value");
        stream.println(gauge.getValue());
        stream.flush();
    }

    @Override
    public void start(long period, TimeUnit unit) {
        this.startTime = clock.getTime();
        super.start(period, unit);
    }

    @Override
    public void shutdown() {
        try {
            super.shutdown();
        } finally {
            for (PrintStream out : streamMap.values()) {
                try {
                    out.close();
                } catch (Throwable t) {
                    LOGGER.warn("Failed to close stream", t);
                }
            }
        }
    }

    private PrintStream getPrintStream(String metricName, String header)
            throws IOException {
        PrintStream stream;
        synchronized (streamMap) {
            stream = streamMap.get(metricName);
            if (stream == null) {
                stream = createStreamForMetric(metricName);
                streamMap.put(metricName, stream);
                stream.println(header);
            }
        }
        return stream;
    }
}
