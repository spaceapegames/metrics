package com.yammer.metrics.core;

import com.yammer.metrics.stats.ExponentiallyDecayingSample;
import com.yammer.metrics.stats.Sample;
import com.yammer.metrics.stats.UniformSample;

/**
 * A metric which calculates the distribution of a value.
 *
 * @see <a href="http://www.johndcook.com/standard_deviation.html">Accurately computing running
 *      variance</a>
 */
public interface Histogram extends Metric, Sampling, Summarizable {
    /**
     * Clears all recorded values.
     */
    void clear();

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    void update(int value);

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    void update(long value);

    /**
     * Returns the number of values recorded.
     *
     * @return the number of values recorded
     */
    long getCount();

    /**
     * The type of sampling the histogram should be performing.
     */
    enum SampleType {
        /**
         * Uses a uniform sample of 1028 elements, which offers a 99.9% confidence level with a 5%
         * margin of error assuming a normal distribution.
         */
        UNIFORM {
            @Override
            public Sample newSample() {
                return new UniformSample(DEFAULT_SAMPLE_SIZE);
            }
        },

        /**
         * Uses an exponentially decaying sample of 1028 elements, which offers a 99.9% confidence
         * level with a 5% margin of error assuming a normal distribution, and an alpha factor of
         * 0.015, which heavily biases the sample to the past 5 minutes of measurements.
         */
        BIASED {
            @Override
            public Sample newSample() {
                return new ExponentiallyDecayingSample(DEFAULT_SAMPLE_SIZE, DEFAULT_ALPHA);
            }
        };

        private static final int DEFAULT_SAMPLE_SIZE = 1028;
        private static final double DEFAULT_ALPHA = 0.015;

        public abstract Sample newSample();
    }
}
