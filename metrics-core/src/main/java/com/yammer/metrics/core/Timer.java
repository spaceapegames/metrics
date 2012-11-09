package com.yammer.metrics.core;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A timer metric which aggregates timing durations and provides duration statistics, plus
 * throughput statistics via {@link Meter}.
 */
public interface Timer extends Metered, Sampling, Summarizable {
    /**
     * Returns the timer's duration scale unit.
     *
     * @return the timer's duration scale unit
     */
    TimeUnit getDurationUnit();

    /**
     * Adds a recorded duration.
     *
     * @param duration the length of the duration
     * @param unit     the scale unit of {@code duration}
     */
    void update(long duration, TimeUnit unit);

    /**
     * Times and records the duration of event.
     *
     * @param event a {@link Callable} whose {@link Callable#call()} method implements a process
     *              whose duration should be timed
     * @param <T>   the type of the value returned by {@code event}
     * @return the value returned by {@code event}
     * @throws Exception if {@code event} throws an {@link Exception}
     */
    <T> T time(Callable<T> event) throws Exception;

    /**
     * Returns a timing {@link TimerContext}, which measures an elapsed time in nanoseconds.
     *
     * @return a new {@link TimerContext}
     */
    TimerContext time();
}
