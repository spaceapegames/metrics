package com.yammer.metrics.jetty;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
        this.duration = registry.add(Metrics.name(SslSelectChannelConnector.class,
                                                  "connection-duration",
                                                  Integer.toString(port)),
                                     Metrics.timer());
        this.accepts = registry.add(Metrics.name(SslSelectChannelConnector.class,
                                                 "accepts",
                                                 Integer.toString(port)),
                                    Metrics.meter());
        this.connects = registry.add(Metrics.name(SslSelectChannelConnector.class,
                                                  "connects",
                                                  Integer.toString(port)),
                                     Metrics.meter());
        this.disconnects = registry.add(Metrics.name(SslSelectChannelConnector.class,
                                                     "disconnects",
                                                     Integer.toString(port)),
                                        Metrics.meter());
        this.connections = registry.add(Metrics.name(SslSelectChannelConnector.class,
                                                     "active-connections",
                                                     Integer.toString(port)),
                                        Metrics.counter());
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
