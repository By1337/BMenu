package dev.by1337.bmenu.loader;

import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.registry.RegistryLike;
import dev.by1337.yaml.codec.DataResult;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;

public class MenuLoader implements Listener {
    private final MenuCodecRegistry menuCodecRegistry = new MenuCodecRegistry();
    private final RegistryLike<MenuConfig> menus = new RegistryLike<>();
    private final File homeDir;
    private final Plugin plugin;
    private final Logger logger;
    private BukkitTask ticker;

    public MenuLoader(File homeDir, Plugin plugin) {
        this.homeDir = homeDir;
        this.plugin = plugin;
        logger = plugin.getSLF4JLogger();
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
        // long nanos = System.nanoTime();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof Menu menu) {
                if (menu.loader() == this) {
                    menu.tick();
                }
            }
        });
        //  System.out.println("Tick " + (System.nanoTime() - nanos) / 1_000_000D + " ms");
    }
    public void enable(){
        startTicker();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadMenus();
    }
    public void reload(){
        closeAllOpenMenus();
        loadMenus();
    }

    public void disable(){
        closeAllOpenMenus();
        menus.clear();
        ticker.cancel();
        HandlerList.unregisterAll(this);
    }

    public void loadMenus() {
        menus.clear();
        recursiveLoad(homeDir);
    }

    public void closeAllOpenMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof Menu m && m.loader() == this) {
                player.closeInventory();
            }
        }
    }

    private void recursiveLoad(File f) {
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                recursiveLoad(file);
            }
        } else if (f.getName().endsWith(".yml") || f.getName().endsWith(".yaml")) {
            try {
                MenuConfigDecoder decoder = new MenuConfigDecoder(f, this, codecRegistry());
                DataResult<? extends MenuConfig> res = decoder.decode();
                if (res.hasError()) {
                    logger.error(res.error());
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
        MenuConfig cfg = menus.get(menuId);
        if (cfg == null) {
            throw new IllegalArgumentException("Menu not found: " + menuId);
        }
        return cfg.create(viewer, previousMenu);
    }
    public Menu create(String menuId, Player viewer, @Nullable Menu previousMenu) {
        MenuConfig cfg = menus.get(menuId);
        if (cfg == null) {
            throw new IllegalArgumentException("Menu not found: " + menuId);
        }
        return cfg.create(viewer, previousMenu);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu && menu.loader() == this) {
            event.setCancelled(true);
            menu.onClick(event);
        }
    }

    @EventHandler
    public void onClick(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu && menu.loader() == this) {
            event.setCancelled(true);
            menu.onClick(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu && menu.loader() == this) {
            menu.onClose(event);
        }
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
