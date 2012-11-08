package com.yammer.metrics.core;

import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Gauge;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

import java.lang.reflect.Method;

/**
 * A value class encapsulating a metric's owning class and name.
 */
public class MetricName {
    private MetricName() { /* singleton */ }

    public static String name(String name, String... names) {
        final StringBuilder builder = new StringBuilder();
        append(builder, name);
        for (String s : names) {
            append(builder, s);
        }
        return builder.toString();
    }

    private static void append(StringBuilder builder, String part) {
        if (part != null && !part.isEmpty()) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(part);
        }
    }

    public static String name(Class<?> klass, String... names) {
        return name(klass.getCanonicalName(), names);
    }

    public static String forTimedMethod(Class<?> klass, Method method, Timed annotation) {
        final String name = annotation.name();
        if (name.isEmpty()) {
            return name(klass, method.getName());
        }
        return name;
    }

    public static String forMeteredMethod(Class<?> klass, Method method, Metered annotation) {
        final String name = annotation.name();
        if (name.isEmpty()) {
            return name(klass, method.getName());
        }
        return name;
    }

    public static String forGaugeMethod(Class<?> klass, Method method, Gauge annotation) {
        final String name = annotation.name();
        if (name.isEmpty()) {
            return name(klass, method.getName());
        }
        return name;
    }

    public static String forExceptionMeteredMethod(Class<?> klass, Method method, ExceptionMetered annotation) {
        final String name = annotation.name();
        if (name.isEmpty()) {
            return name(klass, method.getName(), ExceptionMetered.DEFAULT_NAME_SUFFIX);
        }
        return name;
    }
}
