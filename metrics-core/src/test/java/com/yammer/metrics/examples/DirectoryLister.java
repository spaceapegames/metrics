package com.yammer.metrics.examples;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricGroup;
import com.yammer.metrics.core.Timer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class DirectoryLister {
    private final MetricGroup metrics = Metrics.defaultRegistry().group(DirectoryLister.class);
    private final Counter counter = metrics.counter("directories").build();
    private final Meter meter = metrics.meter("files").measuring("files").build();
    private final Timer timer = metrics.timer("directory-listing").build();
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
