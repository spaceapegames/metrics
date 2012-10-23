package com.yammer.metrics.util.tests;

import com.yammer.metrics.util.PercentGauge;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PercentGaugeTest {
    @Test
    public void returnsAPercentage() throws Exception {
        final PercentGauge gauge = new PercentGauge() {
            @Override
            protected Ratio getRatio() {
                return new Ratio(2, 4);
            }
        };

        assertThat(gauge.getValue(),
                   is(50.0));
    }

    @Test
    public void handlesNaN() throws Exception {
        final PercentGauge gauge = new PercentGauge() {
            @Override
            protected Ratio getRatio() {
                return new Ratio(2, 0);
            }
        };
        
        assertThat(gauge.getValue(),
                   is(Double.NaN));
    }
}
