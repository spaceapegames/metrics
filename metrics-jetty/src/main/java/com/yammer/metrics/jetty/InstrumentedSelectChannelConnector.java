package com.yammer.metrics.jetty;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class InstrumentedSelectChannelConnector extends SelectChannelConnector {
    private final Timer duration;
    private final Meter accepts, connects, disconnects;
    private final Counter connections;

    public InstrumentedSelectChannelConnector(int port) {
        this(Metrics.defaultRegistry(), port);
    }

    public InstrumentedSelectChannelConnector(MetricRegistry registry,
                                              int port) {
        super();
        setPort(port);
        this.duration = registry.add(Metrics.name(SelectChannelConnector.class,
                                                  "connection-duration",
                                                  Integer.toString(port)),
                                     new Timer());
        this.accepts = registry.add(Metrics.name(SelectChannelConnector.class,
                                                 "accepts",
                                                 Integer.toString(port)),
                                    new Meter("connections"));
        this.connects = registry.add(Metrics.name(SelectChannelConnector.class,
                                                  "connects",
                                                  Integer.toString(port)),
                                     new Meter("connections"));
        this.disconnects = registry.add(Metrics.name(SelectChannelConnector.class,
                                                     "disconnects",
                                                     Integer.toString(port)),
                                        new Meter("connections"));
        this.connections = registry.add(Metrics.name(SelectChannelConnector.class,
                                                     "active-connections",
                                                     Integer.toString(port)),
                                        new Counter());
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
