package com.yammer.metrics.util;

import com.yammer.metrics.core.Gauge;

import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;

/**
 * A gauge which measures the ratio of one value to another.
 * <p/>
 * If the denominator is zero, not a number, or infinite, the resulting ratio is not a number.
 */
public abstract class RatioGauge extends Gauge<Double> {
    public static class Ratio {
        private final double numerator;
        private final double denominator;

        public Ratio(double numerator, double denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }

        public double getNumerator() {
            return numerator;
        }

        public double getDenominator() {
            return denominator;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final Ratio ratio = (Ratio) o;

            return Double.compare(ratio.denominator, denominator) == 0 &&
                    Double.compare(ratio.numerator, numerator) == 0;

        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = numerator != +0.0d ? Double.doubleToLongBits(numerator) : 0L;
            result = (int) (temp ^ (temp >>> 32));
            temp = denominator != +0.0d ? Double.doubleToLongBits(denominator) : 0L;
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return numerator + "/" + denominator;
        }
    }

    /**
     * Returns the current value of the ratio.
     *
     * @return the current value of the ratio
     */
    protected abstract Ratio getRatio();

    @Override
    public Double getValue() {
        final Ratio ratio = getRatio();
        final double d = ratio.getDenominator();
        if (isNaN(d) || isInfinite(d) || d == 0.0) {
            return Double.NaN;
        }
        return ratio.getNumerator() / d;
    }
}
