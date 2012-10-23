package com.yammer.metrics.log4j;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricGroup;
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
        final MetricGroup metrics = registry.group(Appender.class);
        this.all = metrics.meter("all").measuring("statements").build();
        this.trace = metrics.meter("trace").measuring("statements").build();
        this.debug = metrics.meter("debug").measuring("statements").build();
        this.info = metrics.meter("info").measuring("statements").build();
        this.warn = metrics.meter("warn").measuring("statements").build();
        this.error = metrics.meter("error").measuring("statements").build();
        this.fatal = metrics.meter("fatal").measuring("statements").build();
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
