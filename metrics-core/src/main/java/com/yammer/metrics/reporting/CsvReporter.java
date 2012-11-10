package com.yammer.metrics.reporting;

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
     * The context used to output metrics.
     */
    interface Context {
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
    private final TimeUnit durationUnit;
    private final Clock clock;
    private long startTime;

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
                       TimeUnit durationUnit,
                       Clock clock) {
        super(metricRegistry, "csv-reporter");
        this.durationUnit = durationUnit;
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
     * Returns an opened {@link PrintStream} for the given metric name which outputs data
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
                            stream.print(clock.getTime());
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
                "timestamp,count,1 min rate,mean rate,5 min rate,15 min rate");
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
        final PrintStream stream = context.getStream("timestamp,count");
        stream.println(counter.getCount());
        stream.flush();
    }

    @Override
    public void processHistogram(String name, Histogram histogram, Context context) throws IOException {
        final PrintStream stream = context.getStream("timestamp,min,max,mean,median,stddev,95%,99%,99.9%");
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
        final PrintStream stream = context.getStream("timestamp,min,max,mean,median,stddev,95%,99%,99.9%,count,1 min rate,mean rate,5 min rate,15 min rate");
        final Snapshot snapshot = timer.getSnapshot();
        stream.append(new StringBuilder()
                              .append(convertFromNS(timer.getMin())).append(',')
                              .append(convertFromNS(timer.getMax())).append(',')
                              .append(convertFromNS(timer.getMean())).append(',')
                              .append(convertFromNS(snapshot.getMedian())).append(',')
                              .append(convertFromNS(timer.getStdDev())).append(',')
                              .append(convertFromNS(snapshot.get95thPercentile())).append(',')
                              .append(convertFromNS(snapshot.get99thPercentile())).append(',')
                              .append(convertFromNS(snapshot.get999thPercentile())).append(',')
                              .append(timer.getCount()).append(',')
                              .append(timer.getOneMinuteRate()).append(',')
                              .append(timer.getMeanRate()).append(',')
                              .append(timer.getFiveMinuteRate()).append(',')
                              .append(timer.getFifteenMinuteRate())
                              .toString())
                .println();
        stream.flush();
    }

    @Override
    public void processGauge(String name, Gauge<?> gauge, Context context) throws IOException {
        final PrintStream stream = context.getStream("timestamp,value");
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

    private double convertFromNS(long ns) {
        return ns / (double) durationUnit.toNanos(1);
    }

    private double convertFromNS(double ns) {
        return ns / (double) durationUnit.toNanos(1);
    }
}
