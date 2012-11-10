package com.yammer.metrics.jdbi.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.jdbi.InstrumentedTimingCollector;
import com.yammer.metrics.jdbi.strategies.NameStrategies;
import com.yammer.metrics.jdbi.strategies.ShortNameStrategy;
import com.yammer.metrics.jdbi.strategies.SmartNameStrategy;
import com.yammer.metrics.jdbi.strategies.StatementNameStrategy;
import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class InstrumentedTimingCollectorTest {
    private final MetricRegistry registry = Metrics.defaultRegistry();

    @Test
    public void updatesTimerForSqlObjects() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass()).when(ctx).getSqlObjectType();
        doReturn(getClass().getMethod("updatesTimerForSqlObjects")).when(ctx).getSqlObjectMethod();

        collector.collect(1, ctx);

        final String name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name,
                   is(Metrics.name(getClass(), "updatesTimerForSqlObjects")));
        assertThat(timer.getMax(),
                   is(1L));
    }

    @Test
    public void updatesTimerForSqlObjectsWithoutMethod() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass()).when(ctx).getSqlObjectType();

        collector.collect(1, ctx);

        final String name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name,
                   is(Metrics.name(getClass(), "SELECT_1")));
        assertThat(timer.getMax(),
                   is(1L));
    }

    @Test
    public void updatesTimerForRawSql() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();

        collector.collect(2, ctx);

        final String name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name,
                   is(Metrics.name("sql", "raw", "SELECT_1")));
        assertThat(timer.getMax(),
                   is(2L));
    }

    @Test
    public void updatesTimerForNoRawSql() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);

        collector.collect(1, ctx);

        final String name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name,
                   is(Metrics.name("sql", "empty")));
        assertThat(timer.getMax(),
                   is(1L));
    }

    @Test
    public void updatesTimerForNonSqlishRawSql() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("don't know what it is but it's not SQL").when(ctx).getRawSql();

        collector.collect(1, ctx);

        final String name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name,
                   is(Metrics.name("sql", "raw", "don_t_know_what_it_is_but_it_s_not_SQL")));
        assertThat(timer.getMax(),
                   is(1L));
    }

    @Test
    public void updatesTimerForContextClass() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass().getName()).when(ctx).getAttribute(NameStrategies.STATEMENT_CLASS);
        doReturn("updatesTimerForContextClass").when(ctx).getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(1, ctx);

        final String name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name,
                   is(Metrics.name(getClass(), "updatesTimerForContextClass")));
        assertThat(timer.getMax(),
                   is(1L));
    }

    @Test
    public void updatesTimerForTemplateFile() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn("foo/bar.stg").when(ctx).getAttribute(NameStrategies.STATEMENT_GROUP);
        doReturn("updatesTimerForTemplateFile").when(ctx).getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(1, ctx);

        final String name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name,
                   is(Metrics.name("foo", "bar", "updatesTimerForTemplateFile")));
        assertThat(timer.getMax(),
                   is(1L));
    }

    @Test
    public void updatesTimerForContextGroupAndName() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn("my-group").when(ctx).getAttribute(NameStrategies.STATEMENT_GROUP);
        doReturn("updatesTimerForContextGroupAndName").when(ctx).getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(1, ctx);

        final String name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name,
                   is(Metrics.name("my-group", "updatesTimerForContextGroupAndName")));
        assertThat(timer.getMax(),
                   is(1L));
    }

    @Test
    public void updatesTimerForContextGroupTypeAndName() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn("my-group").when(ctx).getAttribute(NameStrategies.STATEMENT_GROUP);
        doReturn("my-type").when(ctx).getAttribute(NameStrategies.STATEMENT_TYPE);
        doReturn("updatesTimerForContextGroupTypeAndName").when(ctx).getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(1, ctx);

        final String name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name,
                   is(Metrics.name("my-group", "my-type", "updatesTimerForContextGroupTypeAndName")));
        assertThat(timer.getMax(),
                   is(1L));
    }

    @Test
    public void updatesTimerForShortSqlObjectStrategy() throws Exception {
        final StatementNameStrategy strategy = new ShortNameStrategy("jdbi");
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass()).when(ctx).getSqlObjectType();
        doReturn(getClass().getMethod("updatesTimerForShortSqlObjectStrategy")).when(ctx).getSqlObjectMethod();

        collector.collect(1, ctx);

        final String name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name,
                   is(Metrics.name("jdbi",
                                   getClass().getSimpleName(),
                                   "updatesTimerForShortSqlObjectStrategy")));
        assertThat(timer.getMax(),
                   is(1L));
    }

    @Test
    public void updatesTimerForShortContextClassStrategy() throws Exception {
        final StatementNameStrategy strategy = new ShortNameStrategy("jdbi");
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(registry, strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass().getName()).when(ctx).getAttribute(NameStrategies.STATEMENT_CLASS);
        doReturn("updatesTimerForShortContextClassStrategy").when(ctx).getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(1, ctx);

        final String name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name,
                   is(Metrics.name("jdbi",
                                   getClass().getSimpleName(),
                                   "updatesTimerForShortContextClassStrategy")));
        assertThat(timer.getMax(),
                   is(1L));
    }
}
