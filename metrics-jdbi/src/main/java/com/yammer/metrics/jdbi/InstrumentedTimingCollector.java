package com.yammer.metrics.jdbi;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.jdbi.strategies.SmartNameStrategy;
import com.yammer.metrics.jdbi.strategies.StatementNameStrategy;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.TimingCollector;

import java.util.concurrent.TimeUnit;

/**
 * A {@link TimingCollector} implementation for JDBI which uses the SQL objects' class names and
 * method names for millisecond-precision timers.
 */
public class InstrumentedTimingCollector implements TimingCollector {
    private final MetricRegistry registry;
    private final StatementNameStrategy statementNameStrategy;
    private final TimeUnit durationUnit;
    private final TimeUnit rateUnit;

    public InstrumentedTimingCollector() {
        this(Metrics.defaultRegistry());
    }

    public InstrumentedTimingCollector(MetricRegistry registry) {
        this(registry, new SmartNameStrategy(), TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    }

    public InstrumentedTimingCollector(MetricRegistry registry,
                                       StatementNameStrategy statementNameStrategy) {
        this(registry, statementNameStrategy, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    }

    public InstrumentedTimingCollector(MetricRegistry registry,
                                       StatementNameStrategy statementNameStrategy,
                                       TimeUnit durationUnit,
                                       TimeUnit rateUnit) {
        this.registry = registry;
        this.statementNameStrategy = statementNameStrategy;
        this.durationUnit = durationUnit;
        this.rateUnit = rateUnit;
    }

    @Override
    public void collect(long elapsedTime, StatementContext ctx) {
        final Timer timer = getTimer(ctx);
        timer.update(elapsedTime, TimeUnit.NANOSECONDS);
    }

    private Timer getTimer(StatementContext ctx) {
        return registry.add(statementNameStrategy.getStatementName(ctx).toString(),
                            new Timer(durationUnit, rateUnit));
    }
}
