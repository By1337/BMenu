package org.by1337.bmenu;

import org.bukkit.plugin.Plugin;
import org.by1337.blib.chat.util.Message;
import org.slf4j.Logger;

import java.io.File;

public class MenuLoader {
    private final File homeDir;
    private final Plugin plugin;
    private final Logger logger;
    private final Message message;

    public MenuLoader(File homeDir, Plugin plugin) {
        this.homeDir = homeDir;
        this.plugin = plugin;
        logger = plugin.getSLF4JLogger();
        message = new Message(plugin.getLogger());
    }

    public File getHomeDir() {
        return homeDir;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Logger getLogger() {
        return logger;
    }

    public Message getMessage() {
        return message;
    }
}
