package dev.by1337.bmenu.loader;

import dev.by1337.bmenu.io.FileWatcher;
import dev.by1337.bmenu.loader.v2.MenuDecoders;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.registry.RegistryLike;
import dev.by1337.bmenu.registry.RegistryShortcut;
import dev.by1337.core.util.misc.Pair;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class MenuLoader implements Listener {
    private final RegistryShortcut<YamlCodec<? extends MenuConfig>> lookupProviders;
    private final RegistryShortcut<MenuConfig> lookupMenus;
    private final MenuCodecRegistry menuCodecRegistry = new MenuCodecRegistry();
    private final RegistryLike<MenuConfig> menus = new RegistryLike<>();
    private final File homeDir;
    private final Plugin plugin;
    private final Logger logger;
    private BukkitTask ticker;
    private @Nullable FileWatcher fileWatcher;
    private final Map<Plugin, MenuSubLoader> subLoaders = new IdentityHashMap<>();
    private final Map<UUID, @Nullable Menu> openedMenus = new HashMap<>();

    public MenuLoader(File homeDir, Plugin plugin) {
        this(homeDir, plugin, false);
    }

    public MenuLoader(File homeDir, Plugin plugin, boolean hotReload) {
        this.homeDir = homeDir;
        this.plugin = plugin;
        logger = plugin.getSLF4JLogger();
        if (hotReload) {
            fileWatcher = new FileWatcher(homeDir, this::onFileChange);
            fileWatcher.startWatching();
        }
        lookupProviders = new RegistryShortcut<>() {
            @Override
            protected YamlCodec<? extends MenuConfig> find(String key) {
                var v = menuCodecRegistry.get(key);
                if (v != null) return v;
                for (MenuSubLoader value : subLoaders.values()) {
                    if (((v = value.menuCodecRegistry.get(key)) != null)) return v;
                }
                return null;
            }

            @Override
            protected YamlCodec<? extends MenuConfig> find(NamespacedKey key) {
                var v = menuCodecRegistry.get(key);
                if (v != null) return v;
                for (MenuSubLoader value : subLoaders.values()) {
                    if (((v = value.menuCodecRegistry.get(key)) != null)) return v;
                }
                return null;
            }
        };
        lookupMenus = new RegistryShortcut<>() {
            @Override
            protected MenuConfig find(String key) {
                var v = menus.get(key);
                if (v != null) return v;
                for (MenuSubLoader value : subLoaders.values()) {
                    if (((v = value.menus.get(key)) != null)) return v;
                }
                return null;
            }

            @Override
            protected MenuConfig find(NamespacedKey key) {
                var v = menus.get(key);
                if (v != null) return v;
                for (MenuSubLoader value : subLoaders.values()) {
                    if (((v = value.menus.get(key)) != null)) return v;
                }
                return null;
            }
        };
    }

    private void onFileChange(Path path) {
        hotReload();
    }

    private void hotReload() {
        if (plugin.getServer().isStopping()) return;
        if (!Bukkit.isPrimaryThread()) {
            plugin.getServer().getScheduler().runTask(plugin, this::hotReload);
            return;
        }

        List<Pair<Player, String>> playerMenu = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof Menu menu) {
                if (menu.loader() == this) {
                    playerMenu.add(Pair.of(player, menu.config().id().asString()));
                }
            }
        }
        reload();
        for (Pair<Player, String> pair : playerMenu) {
            try {
                Menu menu = create(pair.getRight(), pair.getLeft(), null);
                menu.open();
            } catch (Exception e) {
                if (e.getMessage().contains("Menu not found")) {
                    logger.error("Failed to reopen menu {} for player {} cuz menu not found", pair.getRight(), pair.getLeft().getName());
                } else {
                    logger.error("Failed to reopen menu {} for player {}", pair.getRight(), pair.getLeft().getName(), e);
                }
            }
        }
    }

    private void startTicker() {
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
        openedMenus.forEach((key, menu) -> {
            if (menu != null) {
                menu.tick();
            }
        });
    }

    public @Nullable Menu getOpenedMenu(Player player) {
        return openedMenus.get(player.getUniqueId());
    }

    public void enable() {
        startTicker();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        menus.clear();
        openedMenus.clear();
        trackPlayer();
        loadMenus();
    }

    public void reload() {
        closeAllOpenMenus();
        menus.clear();
        lookupMenus.clear();
        openedMenus.clear();
        trackPlayer();
        loadMenus();
    }

    private void trackPlayer() {
        Bukkit.getOnlinePlayers().forEach(player -> openedMenus.put(player.getUniqueId(), null));
    }

    @ApiStatus.Internal
    public void onMenuOpen(Menu menu, Player player) {
        openedMenus.put(player.getUniqueId(), menu);
    }

    public void disable() {
        closeAllOpenMenus();
        openedMenus.clear();
        menus.clear();
        lookupMenus.clear();
        ticker.cancel();
        HandlerList.unregisterAll(this);
        if (fileWatcher != null) {
            fileWatcher.stopWatching();
        }
    }

    public void loadMenus() {
        recursiveLoad(homeDir);
    }

    public void closeAllOpenMenus() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof Menu menu && menu.loader() == this) {
                player.closeInventory();
            }
        });
    }

    private void recursiveLoad(File f) {
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                recursiveLoad(file);
            }
        } else if (f.getName().endsWith(".yml") || f.getName().endsWith(".yaml")) {
            try {
                DataResult<? extends MenuConfig> res = MenuDecoders.factory().apply(this).decode(f);
                if (res.hasError()) {
                    logger.error("Errors in {}\n{}", f.getPath(), res.error());
                }
                MenuConfig cfg = res.result();
                if (cfg != null && cfg.id() != null) {
                    menus.register(cfg);
                }
            } catch (Exception t) {
                logger.error("Failed to load menu config File: {}", f.getPath(), t);
            }
        }
    }

    public Menu create(NamespacedKey menuId, Player viewer, @Nullable Menu previousMenu) {
        MenuConfig cfg = lookupMenus.get(menuId);
        if (cfg == null) {
            throw new IllegalArgumentException("Menu not found: " + menuId);
        }
        return cfg.create(viewer, previousMenu);
    }

    public Menu create(String menuId, Player viewer, @Nullable Menu previousMenu) {
        MenuConfig cfg = lookupMenus.get(menuId);
        if (cfg == null) {
            throw new IllegalArgumentException("Menu not found: " + menuId);
        }
        return cfg.create(viewer, previousMenu);
    }

    public YamlCodec<? extends MenuConfig> findMenuCodec(String provider) {
        return lookupProviders.get(provider);
    }

    public MenuConfig findMenuConfig(String name) {
        return lookupMenus.get(name);
    }

    public void registerSubLoader(Plugin owner, MenuSubLoader subLoader) {
        var old = subLoaders.get(owner);
        if (old != null) {
            throw new IllegalStateException("sub loader for " + owner + " is already registered " + old + " new " + subLoader);
        }
        subLoaders.put(owner, subLoader);
        lookupProviders.clear();
        lookupMenus.clear();
    }

    public void unregisterSubLoader(Plugin owner) {
        subLoaders.remove(owner);
        lookupProviders.clear();
        lookupMenus.clear();
    }

    void onReload(MenuSubLoader subLoader) {
        lookupProviders.clear();
        lookupMenus.clear();
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player pl) {
            var m = openedMenus.get(pl.getUniqueId());
            if (m != null) {
                event.setCancelled(true);
                m.onClick(event);
            }
        }
    }

    @EventHandler
    void onClick(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player pl) {
            var m = openedMenus.get(pl.getUniqueId());
            if (m != null) {
                event.setCancelled(true);
                m.onClick(event);
            }
        }
    }

    @EventHandler
    void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player p) {
            var old = openedMenus.put(p.getUniqueId(), null);
            if (old != null) old.onClose(event);
        }
    }

    @EventHandler
    void onJoin(PlayerJoinEvent event) {
        openedMenus.put(event.getPlayer().getUniqueId(), null);
    }

    @EventHandler
    void onQuit(PlayerQuitEvent event) {
        var key = event.getPlayer().getUniqueId();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> openedMenus.remove(key), 0L);
    }


    public MenuCodecRegistry codecRegistry() {
        return menuCodecRegistry;
    }

    public RegistryLike<MenuConfig> menus() {
        return menus;
    }

    public File homeDir() {
        return homeDir;
    }

    public Plugin plugin() {
        return plugin;
    }

    public Logger logger() {
        return logger;
    }
}
