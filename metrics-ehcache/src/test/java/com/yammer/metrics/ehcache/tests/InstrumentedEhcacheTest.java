package com.yammer.metrics.ehcache.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.ehcache.InstrumentedEhcache;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InstrumentedEhcacheTest {
    private static final CacheManager MANAGER = CacheManager.create();

    private Ehcache cache;

    @Before
    public void setUp() throws Exception {
        final Cache c = new Cache(new CacheConfiguration("test", 100));
        MANAGER.addCache(c);
        this.cache = InstrumentedEhcache.instrument(c);
    }

    @Test
    public void measuresGetsAndPuts() throws Exception {
        cache.get("woo");

        cache.put(new Element("woo", "whee"));

        final Timer gets = Metrics.defaultRegistry()
                                  .timer()
                                  .forClass(Cache.class)
                                  .named("gets")
                                  .scopedTo("test")
                                  .build();

        assertThat(gets.getCount(),
                   is(1L));

        final Timer puts = Metrics.defaultRegistry()
                                  .timer()
                                  .forClass(Cache.class)
                                  .named("puts")
                                  .scopedTo("test")
                                  .build();

        assertThat(puts.getCount(),
                   is(1L));
    }
}
