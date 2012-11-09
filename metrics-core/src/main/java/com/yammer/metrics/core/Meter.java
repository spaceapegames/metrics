package com.yammer.metrics.core;

/**
 * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
 * exponentially-weighted moving average throughputs.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">EMA</a>
 */
public interface Meter extends Metered {
    /**
     * Mark the occurrence of an event.
     */
    void mark();

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    void mark(long n);
}
