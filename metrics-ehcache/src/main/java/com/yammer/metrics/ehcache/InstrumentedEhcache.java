package com.yammer.metrics.ehcache;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.constructs.EhcacheDecoratorAdapter;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * An instrumented {@link Ehcache} instance.
 */
public class InstrumentedEhcache extends EhcacheDecoratorAdapter {
    /**
     * Instruments the given {@link Ehcache} instance with get and put timers
     * and a set of gauges for Ehcache's built-in statistics:
     * <p/>
     * <table>
     * <tr>
     * <td>{@code hits}</td>
     * <td>The number of times a requested item was found in the
     * cache.</td>
     * </tr>
     * <tr>
     * <td>{@code in-memory-hits}</td>
     * <td>Number of times a requested item was found in the memory
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code off-heap-hits}</td>
     * <td>Number of times a requested item was found in the off-heap
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code on-disk-hits}</td>
     * <td>Number of times a requested item was found in the disk
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code misses}</td>
     * <td>Number of times a requested item was not found in the
     * cache.</td>
     * </tr>
     * <tr>
     * <td>{@code in-memory-misses}</td>
     * <td>Number of times a requested item was not found in the memory
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code off-heap-misses}</td>
     * <td>Number of times a requested item was not found in the
     * off-heap store.</td>
     * </tr>
     * <tr>
     * <td>{@code on-disk-misses}</td>
     * <td>Number of times a requested item was not found in the disk
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code objects}</td>
     * <td>Number of elements stored in the cache.</td>
     * </tr>
     * <tr>
     * <td>{@code in-memory-objects}</td>
     * <td>Number of objects in the memory store.</td>
     * </tr>
     * <tr>
     * <td>{@code off-heap-objects}</td>
     * <td>Number of objects in the off-heap store.</td>
     * </tr>
     * <tr>
     * <td>{@code on-disk-objects}</td>
     * <td>Number of objects in the disk store.</td>
     * </tr>
     * <tr>
     * <td>{@code mean-get-time}</td>
     * <td>The average get time. Because ehcache support JDK1.4.2, each
     * get time uses {@link System#currentTimeMillis()}, rather than
     * nanoseconds. The accuracy is thus limited.</td>
     * </tr>
     * <tr>
     * <td>{@code mean-search-time}</td>
     * <td>The average execution time (in milliseconds) within the last
     * sample period.</td>
     * </tr>
     * <tr>
     * <td>{@code eviction-count}</td>
     * <td>The number of cache evictions, since the cache was created,
     * or statistics were cleared.</td>
     * </tr>
     * <tr>
     * <td>{@code searches-per-second}</td>
     * <td>The number of search executions that have completed in the
     * last second.</td>
     * </tr>
     * <tr>
     * <td>{@code accuracy}</td>
     * <td>A human readable description of the accuracy setting. One of
     * "None", "Best Effort" or "Guaranteed".</td>
     * </tr>
     * </table>
     *
     * <b>N.B.: This enables Ehcache's sampling statistics with an accuracy
     * level of "none."</b>
     *
     * @param cache       an {@link Ehcache} instance
     * @return an instrumented decorator for {@code cache}
     * @see Statistics
     */
    public static Ehcache instrument(Ehcache cache) {
        return instrument(Metrics.defaultRegistry(), cache);
    }

    /**
     * Instruments the given {@link Ehcache} instance with get and put timers
     * and a set of gauges for Ehcache's built-in statistics:
     * <p/>
     * <table>
     * <tr>
     * <td>{@code hits}</td>
     * <td>The number of times a requested item was found in the
     * cache.</td>
     * </tr>
     * <tr>
     * <td>{@code in-memory-hits}</td>
     * <td>Number of times a requested item was found in the memory
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code off-heap-hits}</td>
     * <td>Number of times a requested item was found in the off-heap
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code on-disk-hits}</td>
     * <td>Number of times a requested item was found in the disk
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code misses}</td>
     * <td>Number of times a requested item was not found in the
     * cache.</td>
     * </tr>
     * <tr>
     * <td>{@code in-memory-misses}</td>
     * <td>Number of times a requested item was not found in the memory
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code off-heap-misses}</td>
     * <td>Number of times a requested item was not found in the
     * off-heap store.</td>
     * </tr>
     * <tr>
     * <td>{@code on-disk-misses}</td>
     * <td>Number of times a requested item was not found in the disk
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code objects}</td>
     * <td>Number of elements stored in the cache.</td>
     * </tr>
     * <tr>
     * <td>{@code in-memory-objects}</td>
     * <td>Number of objects in the memory store.</td>
     * </tr>
     * <tr>
     * <td>{@code off-heap-objects}</td>
     * <td>Number of objects in the off-heap store.</td>
     * </tr>
     * <tr>
     * <td>{@code on-disk-objects}</td>
     * <td>Number of objects in the disk store.</td>
     * </tr>
     * <tr>
     * <td>{@code mean-get-time}</td>
     * <td>The average get time. Because ehcache support JDK1.4.2, each
     * get time uses {@link System#currentTimeMillis()}, rather than
     * nanoseconds. The accuracy is thus limited.</td>
     * </tr>
     * <tr>
     * <td>{@code mean-search-time}</td>
     * <td>The average execution time (in milliseconds) within the last
     * sample period.</td>
     * </tr>
     * <tr>
     * <td>{@code eviction-count}</td>
     * <td>The number of cache evictions, since the cache was created,
     * or statistics were cleared.</td>
     * </tr>
     * <tr>
     * <td>{@code searches-per-second}</td>
     * <td>The number of search executions that have completed in the
     * last second.</td>
     * </tr>
     * <tr>
     * <td>{@code accuracy}</td>
     * <td>A human readable description of the accuracy setting. One of
     * "None", "Best Effort" or "Guaranteed".</td>
     * </tr>
     * </table>
     *
     * <b>N.B.: This enables Ehcache's sampling statistics with an accuracy
     * level of "none."</b>
     *
     * @param cache       an {@link Ehcache} instance
     * @param registry    a {@link com.yammer.metrics.core.MetricRegistry}
     * @return an instrumented decorator for {@code cache}
     * @see Statistics
     */
    public static Ehcache instrument(MetricRegistry registry, final Ehcache cache) {
        cache.setSampledStatisticsEnabled(true);
        cache.setStatisticsAccuracy(Statistics.STATISTICS_ACCURACY_NONE);

        final Class<?> klass = cache.getClass();
        registry.add(Metrics.name(klass, "hits", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getCacheHits();
                         }
                     });

        registry.add(Metrics.name(klass, "in-memory-hits", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getInMemoryHits();
                         }
                     });

        registry.add(Metrics.name(klass, "off-heap-hits", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getOffHeapHits();
                         }
                     });

        registry.add(Metrics.name(klass, "on-disk-hits", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getOnDiskHits();
                         }
                     });

        registry.add(Metrics.name(klass, "misses", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getCacheMisses();
                         }
                     });

        registry.add(Metrics.name(klass, "in-memory-misses", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getInMemoryMisses();
                         }
                     });

        registry.add(Metrics.name(klass, "off-heap-misses", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getOffHeapMisses();
                         }
                     });

        registry.add(Metrics.name(klass, "on-disk-misses", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getOnDiskMisses();
                         }
                     });

        registry.add(Metrics.name(klass, "objects", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getObjectCount();
                         }
                     });

        registry.add(Metrics.name(klass, "in-memory-objects", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getMemoryStoreObjectCount();
                         }
                     });

        registry.add(Metrics.name(klass, "off-heap-objects", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getOffHeapStoreObjectCount();
                         }
                     });

        registry.add(Metrics.name(klass, "on-disk-objects", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getDiskStoreObjectCount();
                         }
                     });

        registry.add(Metrics.name(klass, "mean-get-time", cache.getName()),
                     new Gauge<Float>() {
                         @Override
                         public Float getValue() {
                             return cache.getStatistics().getAverageGetTime();
                         }
                     });

        registry.add(Metrics.name(klass, "mean-search-time", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getAverageSearchTime();
                         }
                     });

        registry.add(Metrics.name(klass, "eviction-count", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getEvictionCount();
                         }
                     });

        registry.add(Metrics.name(klass, "searches-per-second", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getSearchesPerSecond();
                         }
                     });

        registry.add(Metrics.name(klass, "writer-queue-size", cache.getName()),
                     new Gauge<Long>() {
                         @Override
                         public Long getValue() {
                             return cache.getStatistics().getWriterQueueSize();
                         }
                     });

        registry.add(Metrics.name(klass, "accuracy", cache.getName()),
                     new Gauge<String>() {
                         @Override
                         public String getValue() {
                             return cache.getStatistics()
                                         .getStatisticsAccuracyDescription();
                         }
                     });

        return new InstrumentedEhcache(registry, cache);
    }

    private final Timer getTimer, putTimer;

    private InstrumentedEhcache(MetricRegistry registry, Ehcache cache) {
        super(cache);
        this.getTimer = registry.add(Metrics.name(cache.getClass(), "get", cache.getName()),
                                     new Timer(TimeUnit.MICROSECONDS));
        this.putTimer = registry.add(Metrics.name(cache.getClass(), "put", cache.getName()),
                                     new Timer(TimeUnit.MICROSECONDS));
    }

    @Override
    public Element get(Object key) throws IllegalStateException, CacheException {
        final TimerContext ctx = getTimer.time();
        try {
            return underlyingCache.get(key);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public Element get(Serializable key) throws IllegalStateException, CacheException {
        final TimerContext ctx = getTimer.time();
        try {
            return underlyingCache.get(key);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public void put(Element element) throws IllegalArgumentException, IllegalStateException, CacheException {
        final TimerContext ctx = putTimer.time();
        try {
            underlyingCache.put(element);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public void put(Element element, boolean doNotNotifyCacheReplicators) throws IllegalArgumentException, IllegalStateException, CacheException {
        final TimerContext ctx = putTimer.time();
        try {
            underlyingCache.put(element, doNotNotifyCacheReplicators);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public Element putIfAbsent(Element element) throws NullPointerException {
        final TimerContext ctx = putTimer.time();
        try {
            return underlyingCache.putIfAbsent(element);
        } finally {
            ctx.stop();
        }
    }
}
