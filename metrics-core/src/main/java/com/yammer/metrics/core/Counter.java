package com.yammer.metrics.core;

/**
 * An incrementing and decrementing counter metric.
 */
public interface Counter extends Metric {
    /**
     * Increment the counter by one.
     */
    void inc();

    /**
     * Increment the counter by {@code n}.
     *
     * @param n the amount by which the counter will be increased
     */
    void inc(long n);

    /**
     * Decrement the counter by one.
     */
    void dec();

    /**
     * Decrement the counter by {@code n}.
     *
     * @param n the amount by which the counter will be increased
     */
    void dec(long n);

    /**
     * Returns the counter's current value.
     *
     * @return the counter's current value
     */
    long getCount();

    /**
     * Resets the counter to 0.
     */
    void clear();
}
