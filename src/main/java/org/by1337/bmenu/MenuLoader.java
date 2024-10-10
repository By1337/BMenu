package org.by1337.bmenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.Plugin;
import org.by1337.blib.chat.util.Message;
import org.by1337.blib.util.SpacedNameKey;
import org.by1337.bmenu.factory.MenuFactory;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;

public class MenuLoader implements Listener {
    private final MenuRegistry registry;
    private final File homeDir;
    private final Plugin plugin;
    private final Logger logger;
    private final Message message;
    private final Map<SpacedNameKey, MenuConfig> menuConfigs;
    private final Map<String, MenuConfig> menuConfigsLookupByName;
    private final Map<String, Integer> menuNameToMenuCount = new HashMap<>();

    public MenuLoader(File homeDir, Plugin plugin) {
        this.homeDir = homeDir;
        homeDir.mkdirs();
        this.plugin = plugin;
        logger = plugin.getSLF4JLogger();
        message = new Message(plugin.getLogger());
        registry = new MenuRegistry();
        registry.merge(MenuRegistry.DEFAULT_REGISTRY);
        menuConfigs = new HashMap<>();
        menuConfigsLookupByName = new HashMap<>();
    }

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void close() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof Menu) {
                player.closeInventory();
            }
        }
        HandlerList.unregisterAll(this);
    }

    public void loadMenus() {
        for (File file : Objects.requireNonNull(homeDir.listFiles(), "Menu folder isn't exists")) {
            if (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml")) {
                try {
                    MenuConfig config = MenuFactory.load(file, this);
                    if (config.getId() != null) {
                        registerMenu(config);
                    }
                } catch (Throwable t) {
                    logger.warn("Failed to load menu config", t);
                }
            }
        }
    }

    public Menu findAndCreate(SpacedNameKey menuId, Player viewer, @Nullable Menu previousMenu) {
        MenuConfig menuConfig = findMenu(menuId);
        if (menuConfig == null) {
            menuConfig = findMenuByName(menuId.getName().getName());
            if (menuNameToMenuCount.getOrDefault(menuId.getName().getName(), 0) > 1) {
                throw new IllegalStateException("Unambiguous menu call " + menuId.getName().getName() + " Use the full menu name <namespace>:<id>");
            }
        }
        if (menuConfig == null) {
            throw new IllegalArgumentException("Menu not found: " + menuId);
        }
        return findAndCreate(menuConfig, viewer, previousMenu);
    }

    public Menu findAndCreate(MenuConfig config, Player viewer, @Nullable Menu previousMenu) {
        if (config.getProvider() == null) {
            throw new IllegalArgumentException("Menu provider is null");
        }
        MenuRegistry.MenuCreator creator = registry.findCreator(config.getProvider());
        if (creator == null) {
            creator = registry.findCreatorByName(config.getProvider().getName().getName());
        }
        if (creator == null) {
            throw new IllegalArgumentException("Unknown menu provider " + config.getProvider().getName().getName());
        }
        return creator.createMenu(config, viewer, previousMenu);
    }

    @Nullable
    public MenuConfig findMenuByName(String name) {
        return menuConfigsLookupByName.get(name);
    }

    @Nullable
    public MenuConfig findMenu(SpacedNameKey key) {
        return menuConfigs.get(key);
    }

    public int getMenuCount(){
        return menuConfigs.size();
    }
    public Collection<SpacedNameKey> getMenus(){
        return menuConfigs.keySet();
    }

    public void registerMenu(MenuConfig config) {
        if (config.getId() == null) {
            throw new IllegalArgumentException("Can't register anonymous menu config");
        }
        if (!menuConfigs.containsKey(config.getId())) {
            menuConfigs.put(config.getId(), config);
            menuConfigsLookupByName.put(config.getId().getName().getName(), config);
            int x = menuNameToMenuCount.getOrDefault(config.getId().getName().getName(), 0);
            menuNameToMenuCount.put(config.getId().getName().getName(), ++x);
        } else {
            throw new IllegalArgumentException("Menu config already exists");
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getHolder() instanceof Menu menu) {
            menu.onClick(event);
        }
    }

    @EventHandler
    public void onClick(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu) {
            menu.onClick(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu) {
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

    public Message getMessage() {
        return message;
    }

    public MenuRegistry getRegistry() {
        return registry;
    }
}
