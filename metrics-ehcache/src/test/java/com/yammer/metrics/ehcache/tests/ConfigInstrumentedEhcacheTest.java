package com.yammer.metrics.ehcache.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.core.Timer;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ConfigInstrumentedEhcacheTest {
    private static final CacheManager MANAGER = CacheManager.create();
    private static final MetricRegistry REGISTRY = Metrics.defaultRegistry();

    private Ehcache cache;

    @Before
    public void setUp() throws Exception {
        cache = MANAGER.getEhcache("test-config");
        if (cache == null) { fail("Cache is not set correctly"); }
    }

    @Test
    public void measuresGets() throws Exception {
        cache.get("woo");

        final Timer gets = REGISTRY.add(Metrics.name(Cache.class, "get", "test-config"),
                                        (Timer) null);

        assertThat(gets.getCount(), is(1L));

    }

    @Test
    public void measuresPuts() throws Exception {
        cache.put(new Element("woo", "whee"));

        final Timer puts = REGISTRY.add(Metrics.name(Cache.class, "put", "test-config"),
                                        (Timer) null);

        assertThat(puts.getCount(), is(1L));
    }
}
