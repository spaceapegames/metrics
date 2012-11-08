package com.yammer.metrics.examples;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class DirectoryLister {
    private final MetricsRegistry registry = Metrics.defaultRegistry();
    private final Counter counter = registry.add(MetricName.name(getClass(), "directories"),
                                                 new Counter());
    private final Meter meter = registry.add(MetricName.name(getClass(), "files"),
                                             new Meter("files"));
    private final Timer timer = registry.add(MetricName.name(getClass(), "directory-listing"),
                                             new Timer());
    private final File directory;

    public DirectoryLister(File directory) {
        this.directory = directory;
    }

    public List<File> list() throws Exception {
        counter.inc();
        final File[] list = timer.time(new Callable<File[]>() {
            @Override
            public File[] call() throws Exception {
                return directory.listFiles();
            }
        });
        counter.dec();

        if (list == null) {
            return Collections.emptyList();
        }

        final List<File> result = new ArrayList<File>(list.length);
        for (File file : list) {
            meter.mark();
            result.add(file);
        }
        return result;
    }
}
