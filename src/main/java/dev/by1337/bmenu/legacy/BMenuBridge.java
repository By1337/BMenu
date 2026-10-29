package dev.by1337.bmenu.legacy;

import dev.by1337.core.util.reflect.ClasspathUtil;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Loads the old org.by1337 API from the jar bundled with BMenu. */
@ApiStatus.Internal
public final class BMenuBridge {
    private static final String JAR_NAME = "BMenu-1.7+legacy.jar";
    private static final String RESOURCE = "bridges/" + JAR_NAME;

    private BMenuBridge() {
    }

    public static void bootstrap(Plugin plugin) throws IOException {
        ClasspathUtil.addUrl(plugin, unpack(plugin));
    }

    static Path unpack(Plugin plugin) throws IOException {
        Path directory = plugin.getDataFolder().toPath().resolve(".bridges");
        Path file = directory.resolve(JAR_NAME);
        if (!Files.isRegularFile(file)) {
            Files.createDirectories(directory);
            try (var input = plugin.getResource(RESOURCE)) {
                if (input == null) throw new IOException("Unable to find " + RESOURCE);
                Path temporary = Files.createTempFile(directory, "BMenu-legacy-", ".tmp");
                try {
                    Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING);
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    Files.deleteIfExists(temporary);
                }
            }
        }
        return file;
    }
}
