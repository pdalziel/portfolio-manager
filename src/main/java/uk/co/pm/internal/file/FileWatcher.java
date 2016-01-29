package uk.co.pm.internal.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.pm.internal.ApplicationInitializationException;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher {

    private static final Logger logger = LoggerFactory.getLogger(FileWatcher.class);
    private static final int EVENT_PROCESSING_INTERVAL = 5;

    private WatchService watchService;
    private List<EventProcessor> eventProcessors = new ArrayList<>();
    private ScheduledExecutorService executorService;

    public FileWatcher() throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.eventProcessors = new ArrayList<>();
        this.executorService = Executors.newScheduledThreadPool(1);
    }

    public void register(Path directory, FileEventHandler eventHandler) {
        if (!Files.isDirectory(directory)) {
            throw new ApplicationInitializationException(directory + " is not a directory");
        }

        try {
            WatchKey key = directory.register(this.watchService, ENTRY_CREATE);
            this.eventProcessors.add(new EventProcessor(directory, key, eventHandler));
        } catch (IOException e) {
            throw new ApplicationInitializationException("Failed to register event handler for " + directory, e);
        }
        logger.debug("Registered creation event handler for directory " + directory);
    }

    private void processEvents() {
        logger.debug("Beginning event processing cycle");
        for (EventProcessor eventProcessor : eventProcessors) {
            eventProcessor.processEvents();
        }
        logger.debug("Completed event processing cycle");
    }

    public void start() {
        this.executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                processEvents();
            }
        }, 0, EVENT_PROCESSING_INTERVAL, TimeUnit.SECONDS);
    }

    public void shutdown() {
        this.executorService.shutdown();
        try {
            this.watchService.close();
        } catch (IOException e) {
            logger.error("Failed to close down file watcher service", e);
        }
    }

    private class EventProcessor {

        private WatchKey watchKey;
        private FileEventHandler eventHandler;

        public EventProcessor(Path directory, WatchKey watchKey, FileEventHandler eventHandler) {
            this.watchKey = watchKey;
            this.eventHandler = eventHandler;
        }

        public void processEvents() {
            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                if (ENTRY_CREATE.equals(watchEvent.kind())) {
                    processCreationEvent((WatchEvent<Path>) watchEvent);
                }
            }
        }

        private void processCreationEvent(WatchEvent<Path> watchEvent) {
            final Path file = watchEvent.context();
            logger.debug("Processing creation event for file: {}", file);
            try {
                this.eventHandler.handle(file);
            } catch (IOException e) {
                logger.error("Failed to process file:" + file, e);
            }
        }
    }
}
