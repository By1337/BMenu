package dev.by1337.bmenu;

import dev.by1337.bmenu.factory.MenuFactory;
import dev.by1337.bmenu.io.FileWatcher;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.registry.RegistryLike;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuLoader implements Listener {
    private final MenuRegistry registry;
    private final File homeDir;
    private final Plugin plugin;
    private final Logger logger;
    private RegistryLike<MenuConfig> menuRegistry;
    private final String defaultSpace;
    private final @Nullable FileWatcher fileWatcher;
    private BukkitTask ticker;

    public MenuLoader(File homeDir, Plugin plugin) {
        this(homeDir, plugin, false);
    }

    @ApiStatus.Experimental
    public MenuLoader(File homeDir, Plugin plugin, boolean hotReload) {
        if (hotReload) {
            fileWatcher = new FileWatcher(homeDir, this::onFileChange);
            fileWatcher.startWatching();
        } else {
            fileWatcher = null;
        }
        this.homeDir = homeDir;
        homeDir.mkdirs();
        this.plugin = plugin;
        defaultSpace = plugin.getName().toLowerCase();
        logger = plugin.getSLF4JLogger();
        registry = new MenuRegistry();
        registry.merge(MenuRegistry.DEFAULT_REGISTRY);
        menuRegistry = new RegistryLike<>();
    }

    public void startTicker() {
        if (ticker != null && !ticker.isCancelled()) {
            ticker.cancel();
        }
        ticker = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::tick,
                1,
                1
        );
    }

    private void tick() {
        long nanos = System.nanoTime();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof Menu menu) {
                if (menu.getLoader() == this) {
                    menu.tick();
                }
            }
        });
        System.out.println("Tick " + (System.nanoTime() - nanos) / 1_000_000D + " ms");
    }

    private void onFileChange(Path path) {
        plugin.getServer().getScheduler().runTaskLater(plugin, this::hotReload, 4);
    }

    public void hotReload() {
        menuRegistry = new RegistryLike<>();
        loadMenus();

        List<Menu> toUpdate = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof Menu m && m.getLoader() == this) {
                toUpdate.add(m);
            }
        }
        toUpdate.forEach(Menu::close);

        for (Menu menu : toUpdate) {
            List<Menu> hierarchy = new ArrayList<>();
            Menu m = menu;
            do {
                hierarchy.add(m);
                m = m.getPreviousMenu();
            } while (m != null);

            Menu lastMenu = null;
            for (int i = hierarchy.size() - 1; i >= 0; i--) {
                Menu oldMenu = hierarchy.get(i);
                if (!oldMenu.isSupportsHotReload()) break;
                lastMenu = create(oldMenu.getConfig().getId(), oldMenu.getViewer(), lastMenu);
                lastMenu.onHotReload(oldMenu);
            }
            if (lastMenu != null) {
                lastMenu.open();
            }
        }
        logger.info("The configs have been reloaded!");
    }

    public void reload() {
        closeAllOpenMenus();
        menuRegistry = new RegistryLike<>();
        loadMenus();
    }

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void closeAllOpenMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof Menu m && m.getLoader() == this) {
                player.closeInventory();
            }
        }
    }

    public void close() {
        closeAllOpenMenus();
        HandlerList.unregisterAll(this);
        if (fileWatcher != null) {
            fileWatcher.close();
        }
    }

    public void loadMenus() {
        loadMenus0(homeDir).stream().filter(config -> config.getId() != null).forEach(m -> {
            try {
                registerMenu(m);
            } catch (Exception e) {
                logger.error("Failed to load menu {}! cuz {}", m.getId(), e.getMessage());
            }
        });
    }

    private List<MenuConfig> loadMenus0(File dir) {
        List<MenuConfig> result = new ArrayList<>();
        for (File file : Objects.requireNonNull(dir.listFiles(), "Menu folder isn't exists")) {
            if (file.isDirectory()) {
                result.addAll(loadMenus0(file));
            }
            if (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml")) {
                try {
                    result.add(MenuFactory.load(file, this));
                } catch (Exception t) {
                    logger.error("Failed to load menu config File: {}", file.getPath(), t);
                }
            }
        }
        return result;
    }

    public Menu create(NamespacedKey menuId, Player viewer, @Nullable Menu previousMenu) {
        MenuConfig menuConfig = menuRegistry.get(menuId);
        if (menuConfig == null) {
            throw new IllegalArgumentException("Menu not found: " + menuId);
        }
        return create(menuConfig, viewer, previousMenu);
    }

    public Menu create(String menuId, Player viewer, @Nullable Menu previousMenu) {
        MenuConfig menuConfig = menuRegistry.get(menuId);
        if (menuConfig == null) {
            throw new IllegalArgumentException("Menu not found: " + menuId);
        }
        return create(menuConfig, viewer, previousMenu);
    }


    public Menu create(MenuConfig config, Player viewer, @Nullable Menu previousMenu) {
        if (config.getProvider() == null) {
            throw new IllegalArgumentException("Menu provider is null");
        }
        MenuRegistry.MenuCreator creator = registry.get(config.getProvider());
        if (creator == null) {
            creator = registry.get(config.getProvider().getKey());
        }
        if (creator == null) {
            throw new IllegalArgumentException("Unknown menu provider " + config.getProvider());
        }
        return creator.createMenu(config, viewer, previousMenu);
    }

    public void registerMenu(MenuConfig config) {
        if (config.getId() == null) {
            throw new IllegalArgumentException("Can't register anonymous menu config");
        }
        menuRegistry.register(config);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu && menu.getLoader() == this) {
            event.setCancelled(true);
            menu.onClick(event);
        }
    }

    @EventHandler
    public void onClick(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu && menu.getLoader() == this) {
            event.setCancelled(true);
            menu.onClick(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu && menu.getLoader() == this) {
            menu.onClose(event);
        }
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

    public MenuRegistry getRegistry() {
        return registry;
    }

    public RegistryLike<MenuConfig> getMenuRegistry() {
        return menuRegistry;
    }
}
