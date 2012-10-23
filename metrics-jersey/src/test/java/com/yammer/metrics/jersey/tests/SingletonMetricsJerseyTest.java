package com.yammer.metrics.jersey.tests;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.jersey.InstrumentedResourceMethodDispatchAdapter;
import com.yammer.metrics.jersey.tests.resources.InstrumentedResource;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests importing {@link InstrumentedResourceMethodDispatchAdapter} as a singleton
 * in a Jersey {@link com.sun.jersey.api.core.ResourceConfig}
 */
public class SingletonMetricsJerseyTest extends JerseyTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    private MetricRegistry registry;

    @Override
    protected AppDescriptor configure() {
        this.registry = new MetricRegistry();

        final DefaultResourceConfig config = new DefaultResourceConfig();
        config.getSingletons().add(new InstrumentedResourceMethodDispatchAdapter(registry));
        config.getClasses().add(InstrumentedResource.class);

        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void timedMethodsAreTimed() {
        assertThat(resource().path("timed").get(String.class),
                   is("yay"));

        final Timer timer = (Timer) registry.get(new MetricName(InstrumentedResource.class,
                                                                "timed"));
        assertThat(timer.getCount(),
                   is(1L));
    }

    @Test
    public void meteredMethodsAreMetered() {
        assertThat(resource().path("metered").get(String.class),
                   is("woo"));

        final Meter meter = (Meter) registry.get(new MetricName(InstrumentedResource.class,
                                                                "metered"));

        assertThat(meter.getCount(),
                   is(1L));
    }

    @Test
    public void exceptionMeteredMethodsAreExceptionMetered() {
        assertThat(resource().path("exception-metered").get(String.class),
                   is("fuh"));

        try {
            resource().path("exception-metered").queryParam("splode", "true").get(String.class);
            fail("should have thrown a MappableContainerException, but didn't");
        } catch (MappableContainerException e) {
            assertThat(e.getCause(),
                       is(instanceOf(IOException.class)));
        }

        final Meter meter = (Meter) registry.get(new MetricName(InstrumentedResource.class,
                                                                "exceptionMeteredExceptions"));

        assertThat(meter.getCount(),
                   is(1L));
    }
}
