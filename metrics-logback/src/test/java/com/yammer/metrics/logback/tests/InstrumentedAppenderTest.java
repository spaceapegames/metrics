package com.yammer.metrics.logback.tests;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.logback.InstrumentedAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class InstrumentedAppenderTest {
    private Meter all, trace, debug, info, warn, error;
    private ILoggingEvent event;
    private InstrumentedAppender instrumented;

    @Before
    public void setUp() throws Exception {
        this.all = mock(Meter.class);
        this.trace = mock(Meter.class);
        this.debug = mock(Meter.class);
        this.info = mock(Meter.class);
        this.warn = mock(Meter.class);
        this.error = mock(Meter.class);

        this.event = mock(ILoggingEvent.class);
        when(event.getLevel()).thenReturn(Level.INFO);

        final MetricRegistry registry = mock(MetricRegistry.class);
        when(registry.add(eq(Metrics.name(Appender.class, "all")),
                          any(Meter.class))).thenReturn(all);
        when(registry.add(eq(Metrics.name(Appender.class, "trace")),
                          any(Meter.class))).thenReturn(trace);
        when(registry.add(eq(Metrics.name(Appender.class, "debug")),
                          any(Meter.class))).thenReturn(debug);
        when(registry.add(eq(Metrics.name(Appender.class, "info")),
                          any(Meter.class))).thenReturn(info);
        when(registry.add(eq(Metrics.name(Appender.class, "warn")),
                          any(Meter.class))).thenReturn(warn);
        when(registry.add(eq(Metrics.name(Appender.class, "error")),
                          any(Meter.class))).thenReturn(error);

        this.instrumented = new InstrumentedAppender(registry);
        instrumented.start();
    }

    @After
    public void tearDown() throws Exception {
        instrumented.stop();
    }

    @Test
    public void metersTraceEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.TRACE);
        instrumented.doAppend(event);

        verify(trace).mark();
        verify(all).mark();
    }

    @Test
    public void metersDebugEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.DEBUG);
        instrumented.doAppend(event);

        verify(debug).mark();
        verify(all).mark();
    }

    @Test
    public void metersInfoEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.INFO);
        instrumented.doAppend(event);

        verify(info).mark();
        verify(all).mark();
    }

    @Test
    public void metersWarnEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.WARN);
        instrumented.doAppend(event);

        verify(warn).mark();
        verify(all).mark();
    }

    @Test
    public void metersErrorEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.ERROR);
        instrumented.doAppend(event);

        verify(error).mark();
        verify(all).mark();
    }
}
