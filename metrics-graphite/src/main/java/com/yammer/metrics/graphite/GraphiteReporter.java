package com.yammer.metrics.graphite;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.reporting.MetricDispatcher;
import com.yammer.metrics.stats.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.Thread.State;
import java.net.Socket;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;


/**
 * A simple reporter which sends out application metrics to a <a href="http://graphite.wikidot.com/faq">Graphite</a>
 * server periodically.
 */
public class GraphiteReporter extends AbstractPollingReporter implements MetricProcessor<Long> {
    private static final Logger LOG = LoggerFactory.getLogger(GraphiteReporter.class);
    protected final String prefix;
    protected final MetricPredicate predicate;
    protected final Locale locale = Locale.US;
    protected final MetricDispatcher dispatcher = new MetricDispatcher();
    protected final Clock clock;
    protected final SocketProvider socketProvider;
    private final TimeUnit durationUnit;
    protected final VirtualMachineMetrics vm;
    protected Writer writer;
    public boolean printVMMetrics = true;

    /**
     * Creates a new {@link GraphiteReporter}.
     *
     * @param host   is graphite server
     * @param port   is port on which graphite server is running
     * @param prefix is prepended to all names reported to graphite
     * @throws IOException if there is an error connecting to the Graphite server
     */
    public GraphiteReporter(String host, int port, String prefix) throws IOException {
        this(Metrics.defaultRegistry(), host, port, prefix, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a new {@link GraphiteReporter}.
     *
     * @param metricRegistry the metrics registry
     * @param host            is graphite server
     * @param port            is port on which graphite server is running
     * @param prefix          is prepended to all names reported to graphite
     * @param durationUnit    the unit to convert durations to
     * @throws IOException if there is an error connecting to the Graphite server
     */
    public GraphiteReporter(MetricRegistry metricRegistry,
                            String host,
                            int port,
                            String prefix,
                            TimeUnit durationUnit) throws IOException {
        this(metricRegistry,
             prefix,
             MetricPredicate.ALL,
             new DefaultSocketProvider(host, port),
             Clock.defaultClock(),
             durationUnit,
             VirtualMachineMetrics.getInstance(),
             "graphite-reporter");
    }

    /**
     * Creates a new {@link GraphiteReporter}.
     *
     * @param metricRegistry the metrics registry
     * @param prefix         is prepended to all names reported to graphite
     * @param predicate      filters metrics to be reported
     * @param socketProvider a {@link SocketProvider} instance
     * @param clock          a {@link Clock} instance
     * @param durationUnit   the unit to convert durations to
     * @param vm             a {@link VirtualMachineMetrics} instance
     * @throws IOException if there is an error connecting to the Graphite server
     */
    public GraphiteReporter(MetricRegistry metricRegistry,
                            String prefix,
                            MetricPredicate predicate,
                            SocketProvider socketProvider,
                            Clock clock,
                            TimeUnit durationUnit,
                            VirtualMachineMetrics vm,
                            String name) throws IOException {
        super(metricRegistry, name);
        this.socketProvider = socketProvider;
        this.durationUnit = durationUnit;
        this.vm = vm;

        this.clock = clock;

        if (prefix != null) {
            // Pre-append the "." so that we don't need to make anything conditional later.
            this.prefix = prefix + ".";
        } else {
            this.prefix = "";
        }
        this.predicate = predicate;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = this.socketProvider.get();
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            final long epoch = clock.getTime() / 1000;
            if (this.printVMMetrics) {
                printVmMetrics(epoch);
            }
            printRegularMetrics(epoch);
            writer.flush();
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error writing to Graphite", e);
            } else {
                LOG.warn("Error writing to Graphite: {}", e.getMessage());
            }
            if (writer != null) {
                try {
                    writer.flush();
                } catch (IOException e1) {
                    LOG.error("Error while flushing writer:", e1);
                }
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOG.error("Error while closing socket:", e);
                }
            }
            writer = null;
        }
    }

    protected void printRegularMetrics(final Long epoch) {
        for (Entry<String, Metric> entry : getMetricRegistry().filter(predicate)) {
            final String name = entry.getKey();
            final Metric metric = entry.getValue();
            if (metric != null) {
                try {
                    dispatcher.dispatch(metric, name, this, epoch);
                } catch (Exception ignored) {
                    LOG.error("Error printing regular metrics:", ignored);
                }
            }
        }
    }

    protected void sendInt(long timestamp, String name, String valueName, long value) {
        sendToGraphite(timestamp, name, valueName + " " + String.format(locale, "%d", value));
    }

    protected void sendFloat(long timestamp, String name, String valueName, double value) {
        sendToGraphite(timestamp, name, valueName + " " + String.format(locale, "%2.2f", value));
    }

    protected void sendObjToGraphite(long timestamp, String name, String valueName, Object value) {
        sendToGraphite(timestamp, name, valueName + " " + String.format(locale, "%s", value));
    }

    protected void sendToGraphite(long timestamp, String name, String value) {
        try {
            if (!prefix.isEmpty()) {
                writer.write(prefix);
            }
            writer.write(sanitizeString(name));
            writer.write('.');
            writer.write(value);
            writer.write(' ');
            writer.write(Long.toString(timestamp));
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            LOG.error("Error sending to Graphite:", e);
        }
    }

    protected String sanitizeString(String s) {
        return s.replace(' ', '-');
    }

    @Override
    public void processGauge(String name, Gauge<?> gauge, Long epoch) throws IOException {
        sendObjToGraphite(epoch, name, "value", gauge.getValue());
    }

    @Override
    public void processCounter(String name, Counter counter, Long epoch) throws IOException {
        sendInt(epoch, name, "count", counter.getCount());
    }

    @Override
    public void processMeter(String name, Metered meter, Long epoch) throws IOException {
        sendInt(epoch, name, "count", meter.getCount());
        sendFloat(epoch, name, "meanRate", meter.getMeanRate());
        sendFloat(epoch, name, "1MinuteRate", meter.getOneMinuteRate());
        sendFloat(epoch, name, "5MinuteRate", meter.getFiveMinuteRate());
        sendFloat(epoch, name, "15MinuteRate", meter.getFifteenMinuteRate());
    }

    @Override
    public void processHistogram(String name, Histogram histogram, Long epoch) throws IOException {
        sendInt(epoch, name, "min", histogram.getMin());
        sendInt(epoch, name, "max", histogram.getMax());
        sendInt(epoch, name, "mean", histogram.getMean());
        sendFloat(epoch, name, "stddev", histogram.getStdDev());
        final Snapshot snapshot = histogram.getSnapshot();
        sendInt(epoch, name, "median", snapshot.getMedian());
        sendInt(epoch, name, "75percentile", snapshot.get75thPercentile());
        sendInt(epoch, name, "95percentile", snapshot.get95thPercentile());
        sendInt(epoch, name, "98percentile", snapshot.get98thPercentile());
        sendInt(epoch, name, "99percentile", snapshot.get99thPercentile());
        sendInt(epoch, name, "999percentile", snapshot.get999thPercentile());
    }

    @Override
    public void processTimer(String name, Timer timer, Long epoch) throws IOException {
        processMeter(name, timer, epoch);
        sendFloat(epoch, name, "min", convertFromNS(timer.getMin()));
        sendFloat(epoch, name, "max", convertFromNS(timer.getMax()));
        sendFloat(epoch, name, "mean", convertFromNS(timer.getMean()));
        sendFloat(epoch, name, "stddev", convertFromNS(timer.getStdDev()));
        final Snapshot snapshot = timer.getSnapshot();
        sendFloat(epoch, name, "median", convertFromNS(snapshot.getMedian()));
        sendFloat(epoch, name, "75percentile", convertFromNS(snapshot.get75thPercentile()));
        sendFloat(epoch, name, "95percentile", convertFromNS(snapshot.get95thPercentile()));
        sendFloat(epoch, name, "98percentile", convertFromNS(snapshot.get98thPercentile()));
        sendFloat(epoch, name, "99percentile", convertFromNS(snapshot.get99thPercentile()));
        sendFloat(epoch, name, "999percentile", convertFromNS(snapshot.get999thPercentile()));
    }

    protected void printVmMetrics(long epoch) {
        sendFloat(epoch, "jvm.memory", "heap_usage", vm.getHeapUsage());
        sendFloat(epoch, "jvm.memory", "non_heap_usage", vm.getNonHeapUsage());
        for (Entry<String, Double> pool : vm.getMemoryPoolUsage().entrySet()) {
            sendFloat(epoch, "jvm.memory.memory_pool_usages", sanitizeString(pool.getKey()), pool.getValue());
        }

        sendInt(epoch, "jvm", "daemon_thread_count", vm.getDaemonThreadCount());
        sendInt(epoch, "jvm", "thread_count", vm.getThreadCount());
        sendInt(epoch, "jvm", "uptime", vm.getUptime());
        sendFloat(epoch, "jvm", "fd_usage", vm.getFileDescriptorUsage());

        for (Entry<State, Double> entry : vm.getThreadStatePercentages().entrySet()) {
            sendFloat(epoch, "jvm.thread-states", entry.getKey().toString().toLowerCase(), entry.getValue());
        }

        for (Entry<String, VirtualMachineMetrics.GarbageCollectorStats> entry : vm.getGarbageCollectors().entrySet()) {
            final String name = "jvm.gc." + sanitizeString(entry.getKey());
            sendInt(epoch, name, "time", entry.getValue().getTime(TimeUnit.MILLISECONDS));
            sendInt(epoch, name, "runs", entry.getValue().getRuns());
        }
    }

    private double convertFromNS(long ns) {
        return ns / (double) durationUnit.toNanos(1);
    }

    private double convertFromNS(double ns) {
        return ns / (double) durationUnit.toNanos(1);
    }

    public static class DefaultSocketProvider implements SocketProvider {

        private final String host;
        private final int port;

        public DefaultSocketProvider(String host, int port) {
            this.host = host;
            this.port = port;

        }

        @Override
        public Socket get() throws Exception {
            return new Socket(this.host, this.port);
        }

    }
}
