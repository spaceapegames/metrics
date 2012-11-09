package com.yammer.metrics.jetty;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.yammer.metrics.Metrics.name;

public class InstrumentedSslSelectChannelConnector extends SslSelectChannelConnector {
    private final Timer duration;
    private final Meter accepts, connects, disconnects;
    private final Counter connections;

    public InstrumentedSslSelectChannelConnector(int port) {
        this(Metrics.defaultRegistry(), port);
    }

    public InstrumentedSslSelectChannelConnector(MetricRegistry registry,
                                                 int port) {
        super();
        setPort(port);
        this.duration = registry.timer(name(SslSelectChannelConnector.class,
                                            Integer.toString(port),
                                            "connection-duration"));
        this.accepts = registry.meter(name(SslSelectChannelConnector.class,
                                           Integer.toString(port),
                                           "accepts"));
        this.connects = registry.meter(name(SslSelectChannelConnector.class,
                                            Integer.toString(port),
                                            "connects"));
        this.disconnects = registry.meter(name(SslSelectChannelConnector.class,
                                               Integer.toString(port),
                                               "disconnects"));
        this.connections = registry.counter(name(SslSelectChannelConnector.class,
                                                 Integer.toString(port),
                                                 "active-connections"));
    }

    @Override
    public void accept(int acceptorID) throws IOException {
        super.accept(acceptorID);
        accepts.mark();
    }

    @Override
    protected void connectionOpened(Connection connection) {
        connections.inc();
        super.connectionOpened(connection);
        connects.mark();
    }

    @Override
    protected void connectionClosed(Connection connection) {
        super.connectionClosed(connection);
        disconnects.mark();
        final long duration = System.currentTimeMillis() - connection.getTimeStamp();
        this.duration.update(duration, TimeUnit.MILLISECONDS);
        connections.dec();
    }
}
