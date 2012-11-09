package com.yammer.metrics.log4j;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
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
        this.all = registry.add(Metrics.name(Appender.class, "all"), Metrics.meter());
        this.trace = registry.add(Metrics.name(Appender.class, "trace"), Metrics.meter());
        this.debug = registry.add(Metrics.name(Appender.class, "debug"), Metrics.meter());
        this.info = registry.add(Metrics.name(Appender.class, "info"), Metrics.meter());
        this.warn = registry.add(Metrics.name(Appender.class, "warn"), Metrics.meter());
        this.error = registry.add(Metrics.name(Appender.class, "error"), Metrics.meter());
        this.fatal = registry.add(Metrics.name(Appender.class, "fatal"), Metrics.meter());
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
