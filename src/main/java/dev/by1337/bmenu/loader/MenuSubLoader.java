package dev.by1337.bmenu.loader;

import dev.by1337.bmenu.registry.RegistryLike;
import dev.by1337.yaml.codec.DataResult;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

import java.io.File;

public class MenuSubLoader {
    private final Logger log;
    protected final MenuCodecRegistry menuCodecRegistry = new MenuCodecRegistry();
    protected final RegistryLike<MenuConfig> menus = new RegistryLike<>();

    private final File homeDir;
    private final Plugin plugin;
    private final MenuLoader base;

    public MenuSubLoader(File homeDir, Plugin plugin, MenuLoader base) {
        this.homeDir = homeDir;
        this.plugin = plugin;
        log = plugin.getSLF4JLogger();
        this.base = base;
    }

    public MenuCodecRegistry menuCodecRegistry() {
        return menuCodecRegistry;
    }

    public RegistryLike<MenuConfig> menus() {
        return menus;
    }

    public void reload() {
        base.onReload(this);
        loadMenus();
    }

    public void loadMenus() {
        menus.clear();
        recursiveLoad(homeDir);
    }

    private void recursiveLoad(File f) {
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                recursiveLoad(file);
            }
        } else if (f.getName().endsWith(".yml") || f.getName().endsWith(".yaml")) {
            try {
                MenuConfigDecoder decoder = new MenuConfigDecoder(f, base);
                DataResult<? extends MenuConfig> res = decoder.decode();
                if (res.hasError()) {
                    log.error("Errors in {}\n{}", f.getPath(), res.error());
                }
                MenuConfig cfg = res.result();
                if (cfg != null && cfg.id() != null) {
                    menus.register(cfg);
                }
            } catch (Exception t) {
                log.error("Failed to load menu config File: {}", f.getPath(), t);
            }
        }
    }
}
