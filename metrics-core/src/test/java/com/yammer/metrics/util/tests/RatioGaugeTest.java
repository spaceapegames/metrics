package com.yammer.metrics.util.tests;

import com.yammer.metrics.util.RatioGauge;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class RatioGaugeTest {
    @Test
    public void calculatesTheRatioOfTheNumeratorToTheDenominator() throws Exception {
        final RatioGauge regular = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return new Ratio(2, 4);
            }
        };

        assertThat(regular.getValue(),
                   is(0.5));
    }

    @Test
    public void handlesDivideByZeroIssues() throws Exception {
        final RatioGauge divByZero = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return new Ratio(100, 0);
            }
        };

        assertThat(divByZero.getValue(),
                   is(Double.NaN));
    }

    @Test
    public void handlesInfiniteDenominators() throws Exception {
        final RatioGauge infinite = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return new Ratio(10, Double.POSITIVE_INFINITY);
            }
        };
        
        assertThat(infinite.getValue(),
                   is(Double.NaN));
    }

    @Test
    public void handlesNaNDenominators() throws Exception {
        final RatioGauge nan = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return new Ratio(10, Double.NaN);
            }
        };
        
        assertThat(nan.getValue(),
                   is(Double.NaN));
    }
}
