package org.by1337.bmenu.io;

import org.jetbrains.annotations.ApiStatus;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@ApiStatus.Experimental
@ApiStatus.Internal
public class FileWatcher implements Closeable {
    private final File configPath;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Consumer<Path> onChange;
    private long lastUpdate = 0;

    public FileWatcher(File configDirectory, Consumer<Path> onChange) {
        this.configPath = configDirectory;
        this.onChange = onChange;
    }

    public void startWatching() {
        executor.submit(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                registerListener(configPath, watchService);

                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Path changedFile = (Path) event.context();
                            if (System.currentTimeMillis() - lastUpdate > 150) {
                                onChange.accept(changedFile);
                            }
                            lastUpdate = System.currentTimeMillis();
                        }
                    }
                    key.reset();
                }
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void registerListener(File file, WatchService watchService) throws IOException {
        file.toPath().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        for (File listFile : file.listFiles()) {
            if (listFile.isDirectory()) {
                registerListener(listFile, watchService);
            }
        }
    }

    public void stopWatching() {
        executor.shutdownNow();
    }

    @Override
    public void close() {
        stopWatching();
    }
}
