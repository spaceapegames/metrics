package com.yammer.metrics.jetty;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class InstrumentedBlockingChannelConnector extends BlockingChannelConnector {
    private final Timer duration;
    private final Meter accepts, connects, disconnects;
    private final Counter connections;

    public InstrumentedBlockingChannelConnector(int port) {
        this(Metrics.defaultRegistry(), port);
    }

    public InstrumentedBlockingChannelConnector(MetricsRegistry registry,
                                                int port) {
        super();
        setPort(port);

        final MetricsGroup metrics = registry.group(BlockingChannelConnector.class);
        this.duration = metrics.timer("connection-duration")
                               .scopedTo(Integer.toString(port))
                               .build();
        this.accepts = metrics.meter("accepts")
                              .scopedTo(Integer.toString(port))
                              .measuring("connections")
                              .build();
        this.connects = metrics.meter("connects")
                               .scopedTo(Integer.toString(port))
                               .measuring("connections")
                               .build();
        this.disconnects = metrics.meter("disconnects")
                                  .scopedTo(Integer.toString(port))
                                  .measuring("connections")
                                  .build();
        this.connections = metrics.counter("active-connections")
                                  .scopedTo(Integer.toString(port))
                                  .build();
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
