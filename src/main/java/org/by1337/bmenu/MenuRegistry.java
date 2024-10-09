package org.by1337.bmenu;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.by1337.blib.util.SpacedNameKey;
import org.by1337.bmenu.impl.DefaultMenu;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MenuRegistry {
    public static final MenuRegistry DEFAULT_REGISTRY = new MenuRegistry();

    private final Map<SpacedNameKey, MenuCreator> menus = new HashMap<>();
    private final Map<String, MenuCreator> lookupByName = new HashMap<>();

    public void register(SpacedNameKey id, MenuCreator creator) {
        if (menus.containsKey(id)) {
            throw new IllegalArgumentException("Menu already registered with id " + id);
        }
        menus.put(id, creator);
        lookupByName.put(id.getName().getName(), creator);
    }

    public void merge(MenuRegistry other) {
        for (SpacedNameKey spacedNameKey : other.menus.keySet()) {
            if (!menus.containsKey(spacedNameKey)) {
                menus.put(spacedNameKey, other.menus.get(spacedNameKey));
                lookupByName.put(spacedNameKey.getName().getName(), other.menus.get(spacedNameKey));
            }
        }
    }

    @Nullable
    public MenuCreator findCreator(SpacedNameKey key) {
        return menus.get(key);
    }

    @Nullable
    public MenuCreator findCreatorByName(String name) {
        return lookupByName.get(name);
    }


    public interface MenuCreator {
        Menu createMenu(MenuConfig config, Player viewer, @Nullable Menu previousMenu);
    }

    static {
        Plugin plugin = ((PluginClassLoader) MenuRegistry.class.getClassLoader()).getPlugin();
        DEFAULT_REGISTRY.register(new SpacedNameKey(plugin.getName(), "default"), DefaultMenu::new);
    }
}
