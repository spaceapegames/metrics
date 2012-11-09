package com.yammer.metrics.ehcache.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.ehcache.InstrumentedEhcache;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InstrumentedEhcacheTest {
    private static final CacheManager MANAGER = CacheManager.create();
    private static final MetricRegistry REGISTRY = new MetricRegistry();

    private Ehcache cache;

    @Before
    public void setUp() throws Exception {
        final Cache c = new Cache(new CacheConfiguration("test", 100));
        MANAGER.addCache(c);
        this.cache = InstrumentedEhcache.instrument(REGISTRY, c);
    }

    @After
    public void tearDown() throws Exception {
        MANAGER.removeCache("test");
    }

    @Test
    public void measuresGets() throws Exception {
        cache.get("woo");

        final Timer gets = REGISTRY.timer(Metrics.name(Cache.class, "test", "get"));

        assertThat(gets.getCount(), is(1L));

    }

    @Test
    public void measuresPuts() throws Exception {
        cache.put(new Element("woo", "whee"));

        final Timer puts = REGISTRY.timer(Metrics.name(Cache.class, "test", "put"));

        assertThat(puts.getCount(), is(1L));
    }
}
