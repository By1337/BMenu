package org.by1337.bmenu.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.by1337.blib.configuration.YamlConfig;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;

@ApiStatus.Internal
public class ConfigUtil {
    private static final Plugin INSTANCE = ((PluginClassLoader) ConfigUtil.class.getClassLoader()).getPlugin();

    public static YamlConfig load(String path) {
        return tryRun(() -> new YamlConfig(trySave(path)));
    }

    @CanIgnoreReturnValue
    public static File trySave(String path) {
        path = path.replace('\\', '/');
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        var f = new File(INSTANCE.getDataFolder(), path);
        if (!f.exists()) {
            INSTANCE.saveResource(path, false);
        }
        return f;
    }

    public static <T> T tryRun(ThrowableRunnable<T> runnable) {
        try {
            return runnable.run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface ThrowableRunnable<T> {
        T run() throws Throwable;
    }
}
