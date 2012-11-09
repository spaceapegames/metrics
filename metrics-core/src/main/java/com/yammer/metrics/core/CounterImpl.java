package com.yammer.metrics.core;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The default implementation of {@link Counter}.
 */
public class CounterImpl implements Counter {
    private final AtomicLong count;

    public CounterImpl() {
        this.count = new AtomicLong(0);
    }

    @Override
    public void inc() {
        inc(1);
    }

    @Override
    public void inc(long n) {
        count.addAndGet(n);
    }

    @Override
    public void dec() {
        dec(1);
    }

    @Override
    public void dec(long n) {
        count.addAndGet(0 - n);
    }

    @Override
    public long getCount() {
        return count.get();
    }

    @Override
    public void clear() {
        count.set(0);
    }
}
