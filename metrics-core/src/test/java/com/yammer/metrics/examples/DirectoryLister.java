package com.yammer.metrics.examples;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static com.yammer.metrics.Metrics.*;

public class DirectoryLister {
    private static final Counter DIRECTORIES = metric(name(DirectoryLister.class, "directories"),
                                                      counter());
    private static final Meter FILES = metric(name(DirectoryLister.class, "files"), meter());
    private static final Timer LISTING = metric(name(DirectoryLister.class, "directory-listing"),
                                                timer());
    private final File directory;

    public DirectoryLister(File directory) {
        this.directory = directory;
    }

    public List<File> list() throws Exception {
        DIRECTORIES.inc();
        final File[] list = LISTING.time(new Callable<File[]>() {
            @Override
            public File[] call() throws Exception {
                return directory.listFiles();
            }
        });
        DIRECTORIES.dec();

        if (list == null) {
            return Collections.emptyList();
        }

        final List<File> result = new ArrayList<File>(list.length);
        for (File file : list) {
            FILES.mark();
            result.add(file);
        }
        return result;
    }
}
