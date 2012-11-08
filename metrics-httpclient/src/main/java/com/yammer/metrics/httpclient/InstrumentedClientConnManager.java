package com.yammer.metrics.httpclient;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;

import java.util.concurrent.TimeUnit;

/**
 * A {@link ClientConnectionManager} which monitors the number of open connections.
 */
public class InstrumentedClientConnManager extends PoolingClientConnectionManager {
    public InstrumentedClientConnManager() {
        this(SchemeRegistryFactory.createDefault());
    }

    public InstrumentedClientConnManager(SchemeRegistry registry) {
        this(registry, -1, TimeUnit.MILLISECONDS);
    }

    public InstrumentedClientConnManager(SchemeRegistry registry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit) {
        this(Metrics.defaultRegistry(), registry, connTTL, connTTLTimeUnit);
    }

    public InstrumentedClientConnManager(MetricsRegistry metricsRegistry,
                                         SchemeRegistry registry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit) {
        this(metricsRegistry, registry, connTTL, connTTLTimeUnit, new SystemDefaultDnsResolver());
    }

    public InstrumentedClientConnManager(MetricsRegistry metricsRegistry,
                                         SchemeRegistry schemeRegistry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit,
                                         DnsResolver dnsResolver) {
        super(schemeRegistry, connTTL, connTTLTimeUnit, dnsResolver);
        final Class<?> klass = ClientConnectionManager.class;
        metricsRegistry.add(MetricName.name(klass, "available-connections"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                // this acquires a lock on the connection pool; remove if contention sucks
                return getTotalStats().getAvailable();
            }
        });
        metricsRegistry.add(MetricName.name(klass, "leased-connections"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                // this acquires a lock on the connection pool; remove if contention sucks
                return getTotalStats().getLeased();
            }
        });
        metricsRegistry.add(MetricName.name(klass, "max-connections"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                // this acquires a lock on the connection pool; remove if contention sucks
                return getTotalStats().getMax();
            }
        });
        metricsRegistry.add(MetricName.name(klass, "pending-connections"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                // this acquires a lock on the connection pool; remove if contention sucks
                return getTotalStats().getPending();
            }
        });
    }
}
