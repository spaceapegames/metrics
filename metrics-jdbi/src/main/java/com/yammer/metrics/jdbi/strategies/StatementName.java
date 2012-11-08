package com.yammer.metrics.jdbi.strategies;

import com.yammer.metrics.Metrics;

import java.util.regex.Pattern;

public class StatementName {
    /**
     * Characters safe to be used in JMX names.
     */
    private static final Pattern JMX_SAFE_CHARS = Pattern.compile("[^a-zA-Z0-9_\\.-]");

    public static String getJmxSafeName(String groupName, String typeName, String statementName) {
        return Metrics.name(getJmxSafeName(groupName),
                            getJmxSafeName(typeName),
                            getJmxSafeName(statementName));
    }

    /**
     * Turns an arbitrary string into a JMX safe name.
     *
     * @param name    an arbitrary string
     * @return a JMX-safe name
     */
    private static String getJmxSafeName(String name) {
        final String result = JMX_SAFE_CHARS.matcher(name).replaceAll("_");

        if (result == null || result.length() == 0) {
            return "";
        }

        return (Character.isDigit(result.charAt(0))) ? "_" + result : result;
    }
}
