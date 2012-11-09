package com.yammer.metrics.jetty;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.core.Timer;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.yammer.metrics.Metrics.name;

public class InstrumentedBlockingChannelConnector extends BlockingChannelConnector {
    private final Timer duration;
    private final Meter accepts, connects, disconnects;
    private final Counter connections;

    public InstrumentedBlockingChannelConnector(int port) {
        this(Metrics.defaultRegistry(), port);
    }

    public InstrumentedBlockingChannelConnector(MetricRegistry registry,
                                                int port) {
        super();
        setPort(port);
        this.duration = registry.timer(name(BlockingChannelConnector.class,
                                            "connection-duration",
                                            Integer.toString(port)));
        this.accepts = registry.meter(name(BlockingChannelConnector.class,
                                           "accepts",
                                           Integer.toString(port)));
        this.connects = registry.meter(name(BlockingChannelConnector.class,
                                            "connects",
                                            Integer.toString(port)));
        this.disconnects = registry.meter(name(BlockingChannelConnector.class,
                                               "disconnects",
                                               Integer.toString(port)));
        this.connections = registry.counter(name(BlockingChannelConnector.class,
                                                 "active-connections",
                                                 Integer.toString(port)));
    }

    @Override
    public void accept(int acceptorID) throws IOException, InterruptedException {
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
