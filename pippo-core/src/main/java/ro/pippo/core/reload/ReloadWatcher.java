/*
 * Copyright (C) 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.core.reload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * This class runs a background task that checks directories for new, modified or removed files.
 * It's used for reloading mechanism.
 *
 * @author Decebal Suiu
 */
public class ReloadWatcher implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ReloadWatcher.class);

    public enum Event {
        ENTRY_CREATE,
        ENTRY_MODIFY,
        ENTRY_DELETE
    }

    private static final Map<WatchEvent.Kind<Path>, Event> EVENT_MAP = new HashMap<WatchEvent.Kind<Path>, Event>() {{
        put(ENTRY_CREATE, Event.ENTRY_CREATE);
        put(ENTRY_MODIFY, Event.ENTRY_MODIFY);
        put(ENTRY_DELETE, Event.ENTRY_DELETE);
    }};

    private final Set<Path> dirPaths;
    private Listener listener;
    private ExecutorService executorService;

    private Future<?> watcherTask;
    private WatchService watchService;
    private Map<WatchKey, Path> watchKeyToDirectory;

    private boolean running;

    private ReloadWatcher(Set<Path> dirPaths, Listener listener) {
        this.dirPaths = dirPaths;
        this.listener = listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void start() {
        if (!running) {
            running = true;
            watcherTask = executorService.submit(this);
        }
    }

    public void stop() {
        if (running) {
            running = false;
            executorService.shutdownNow();
            try {
                watchService.close();
            } catch (IOException e) {
                log.error("Cannot close the watch service", e);
            }

            watcherTask.cancel(true);
            watcherTask = null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException("Exception while creating watch service", e);
        }

        watchKeyToDirectory = new HashMap<>();

        for (Path dir : dirPaths) {
            try {
                // register the given directory, and all its sub-directories
                registerDirectory(dir);
            } catch (IOException e) {
                log.error("Not watching '{}'", dir, e);
            }
        }

        while (running) {
            if (Thread.interrupted()) {
                log.info("Directory watcher thread interrupted");
                break;
            }

            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                continue;
            }

            Path dir = watchKeyToDirectory.get(key);
            if (dir == null) {
                log.warn("Watch key not recognized");
                continue;
            }

            // http://stackoverflow.com/a/25221600
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore ?!
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.kind().equals(OVERFLOW)) {
                    break;
                }

                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                WatchEvent.Kind<Path> kind = pathEvent.kind();

                Path path = dir.resolve(pathEvent.context());

                // if directory is created, and watching recursively, then register it and its sub-directories
                if (kind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(path, NOFOLLOW_LINKS)) {
                            registerDirectory(dir);
                        }
                    } catch (IOException e) {
                        // ignore
                    }
                }

                if (running && EVENT_MAP.containsKey(kind)) {
                    listener.onEvent(EVENT_MAP.get(kind), dir, pathEvent.context());
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                watchKeyToDirectory.remove(key);
//                log.warn("'{}' is inaccessible, stopping watch", dir);
                if (watchKeyToDirectory.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
     * Register the given directory, and all its sub-directories.
     */
    private void registerDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                watchKeyToDirectory.put(key, dir);

                return FileVisitResult.CONTINUE;
            }

        });
    }

    public interface Listener {

        void onEvent(Event event, Path dir, Path path);

    }

    public static class Builder {

        private Set<Path> dirPaths = new HashSet<>();
        private ExecutorService executorService;

        public Builder addDirectory(String dirPath) {
            return addDirectory(Paths.get(dirPath));
        }

        public Builder addDirectory(Path dirPath) {
            dirPaths.add(dirPath);

            return this;
        }

        public Builder setExecutorService(ExecutorService executorService) {
            this.executorService = executorService;

            return this;
        }

        public ReloadWatcher build(Listener listener) {
            ReloadWatcher watcher = new ReloadWatcher(dirPaths, listener);
            if (executorService == null) {
                executorService = Executors.newSingleThreadExecutor();
            }
            watcher.executorService = executorService;

            return watcher;
        }

    }

}

