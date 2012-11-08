package com.yammer.metrics.log4j;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricRegistry;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A Log4J {@link Appender} delegate which has seven meters, one for each logging level and one for
 * the total number of statements being logged.
 */
public class InstrumentedAppender extends AppenderSkeleton {
    private final Meter all;
    private final Meter trace;
    private final Meter debug;
    private final Meter info;
    private final Meter warn;
    private final Meter error;
    private final Meter fatal;

    public InstrumentedAppender() {
        this(Metrics.defaultRegistry());
    }

    public InstrumentedAppender(MetricRegistry registry) {
        super();
        this.all = registry.add(MetricName.name(Appender.class, "all"), new Meter("statements"));
        this.trace = registry.add(MetricName.name(Appender.class, "trace"), new Meter("statements"));
        this.debug = registry.add(MetricName.name(Appender.class, "debug"), new Meter("statements"));
        this.info = registry.add(MetricName.name(Appender.class, "info"), new Meter("statements"));
        this.warn = registry.add(MetricName.name(Appender.class, "warn"), new Meter("statements"));
        this.error = registry.add(MetricName.name(Appender.class, "error"), new Meter("statements"));
        this.fatal = registry.add(MetricName.name(Appender.class, "fatal"), new Meter("statements"));
    }

    @Override
    protected void append(LoggingEvent event) {
        all.mark();
        switch (event.getLevel().toInt()) {
            case Level.TRACE_INT:
                trace.mark();
                break;
            case Level.DEBUG_INT:
                debug.mark();
                break;
            case Level.INFO_INT:
                info.mark();
                break;
            case Level.WARN_INT:
                warn.mark();
                break;
            case Level.ERROR_INT:
                error.mark();
                break;
            case Level.FATAL_INT:
                fatal.mark();
                break;
        }
    }

    @Override
    public void close() {
        // nothing doing
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
