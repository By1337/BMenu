package org.by1337.bmenu;

import dev.by1337.yaml.YamlMap;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryType;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.command.CommandList;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.requirement.CommandRequirements;
import org.by1337.bmenu.yaml.CashedYamlMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MenuConfig implements MenuItemLookup, Keyed {
    private final Set<NamespacedKey> supersId;
    private final List<MenuConfig> supers;
    private final @Nullable NamespacedKey id;
    private final @Nullable NamespacedKey provider;
    private final InventoryType invType;
    private final int size;
    private final Set<NamespacedKey> onlyOpenFrom;
    private final Map<String, String> args;
    private final Map<String, MenuItemBuilder> idToItems;
    private final YamlMap yamlConfig;
    private final CashedYamlMap cashedYamlConfig;
    private final MenuLoader loader;
    private final String title;
    private final List<MenuItemBuilder> items;
    private @Nullable Animator.AnimatorContext animation;
    private final CommandList commandList;
    private final Map<String, CommandRequirements> menuEventListeners;
    private Object data;
    private final long clickCooldown;

    private final Map<String, Animator.AnimatorContext> animations;
    private final List<File> fromFiles;

    public MenuConfig(List<MenuConfig> supers, @Nullable NamespacedKey id, @Nullable NamespacedKey provider, InventoryType invType, int size, List<NamespacedKey> onlyOpenFrom, Map<String, String> args, Map<String, MenuItemBuilder> idToItems, YamlMap context, MenuLoader loader, String title, @Nullable Animator.AnimatorContext animation, CommandList commandList, Map<String, CommandRequirements> menuEventListeners, long clickCooldown, Map<String, Animator.AnimatorContext> animations, List<File> fromFiles) {
        this.supers = supers;
        this.id = id;
        this.provider = provider;
        this.invType = invType;
        this.size = size;
        this.onlyOpenFrom = new HashSet<>(onlyOpenFrom);
        this.args = args;
        this.idToItems = idToItems;
        this.clickCooldown = clickCooldown;
        yamlConfig = context;
        cashedYamlConfig = new CashedYamlMap(yamlConfig);

        this.animations = new HashMap<>(animations);
        this.fromFiles = fromFiles;
        this.loader = loader;
        this.title = title;
        this.animation = animation;
        this.commandList = commandList;
        this.menuEventListeners = menuEventListeners;
        supersId = new HashSet<>();
        items = idToItems.values().stream().sorted().toList();
        for (MenuConfig superMenu : supers) {
            if (superMenu.id != null) {
                supersId.add(superMenu.id);
            }
            this.onlyOpenFrom.addAll(superMenu.onlyOpenFrom);
            this.commandList.merge(superMenu.commandList);
            if (superMenu.animation != null) {
                if (this.animation == null) {
                    this.animation = superMenu.animation;
                } else {
                    this.animation.merge(superMenu.animation);
                }
            }
            for (String s : superMenu.animations.keySet()) {
                if (this.animations.containsKey(s)) {
                    this.animations.get(s).merge(superMenu.animations.get(s));
                } else {
                    this.animations.put(s, superMenu.animations.get(s));
                }
            }
        }
    }

    public boolean canOpenFrom(@Nullable Menu menu) {
        if (onlyOpenFrom.isEmpty()) return true;
        if (menu == null || menu.getConfig().id == null) return false;
        if (onlyOpenFrom.contains(menu.getConfig().id)) return true;
        for (NamespacedKey spacedNameKey : menu.getConfig().supersId) {
            if (supersId.contains(spacedNameKey)) return true;
        }
        return false;
    }

    public void generate(Menu menu) {
        for (MenuConfig superMenu : supers) {
            superMenu.generate(menu);
        }
        MenuItem[] matrix = menu.getMatrix();
        int invSize = matrix.length;

        for (MenuItemBuilder builder : items) {
            MenuItem menuItem = null;
            var slots = builder.slots();
            for (int slot : slots) {
                if (slot < 0 || slot >= invSize) continue;
                if (menuItem == null) {
                    menuItem = builder.build(menu);
                    if (menuItem == null) break;
                }
                matrix[slot] = menuItem;
            }
        }
    }

    @Override
    public @Nullable MenuItemBuilder findMenuItem(String name, Menu menu) {
        MenuItemBuilder item = idToItems.get(name);
        if (item != null) return item;
        for (MenuConfig superMenu : supers) {
            if ((item = superMenu.findMenuItem(name, menu)) != null) {
                return item;
            }
        }
        return null;
    }

    public void dump(Path toFolder) {
        dump(toFolder, getSaveName());
    }

    public void dump(Path toFolder, String name) {
        try {
            if (!toFolder.toFile().exists()) {
                toFolder.toFile().mkdirs();
            }
            Files.writeString(toFolder.resolve(name + ".yml"), yamlConfig.saveToString());

            int x = 0;
            for (MenuConfig superMenu : supers) {
                superMenu.dump(toFolder, name + "$" + superMenu.getSaveName() + "$" + x++);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSaveName() {
        return id == null ? "anonymous" : id.getNamespace() + "_" + id.getKey();
    }

    public Map<String, CommandRequirements> getMenuEventListeners() {
        return menuEventListeners;
    }

    public YamlMap getYamlConfig() {
        return yamlConfig;
    }

    public CashedYamlMap getCashedYamlConfig() {
        return cashedYamlConfig;
    }

    public Set<NamespacedKey> getSupersId() {
        return supersId;
    }

    public List<MenuConfig> getSupers() {
        return supers;
    }

    public @Nullable NamespacedKey getId() {
        return id;
    }

    public @Nullable NamespacedKey getProvider() {
        return provider;
    }

    public InventoryType getInvType() {
        return invType;
    }

    public int getSize() {
        return size;
    }

    public Set<NamespacedKey> getOnlyOpenFrom() {
        return onlyOpenFrom;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public Map<String, MenuItemBuilder> getIdToItems() {
        return idToItems;
    }

    public MenuLoader getLoader() {
        return loader;
    }

    public String getTitle() {
        return title;
    }

    public List<MenuItemBuilder> getItems() {
        return items;
    }

    public @Nullable Animator.AnimatorContext getAnimation() {
        return animation;
    }

    public CommandList getCommandList() {
        return commandList;
    }

    public void setAnimation(@Nullable Animator.AnimatorContext animation) {
        this.animation = animation;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String, Animator.AnimatorContext> getAnimations() {
        return animations;
    }

    public List<File> getFromFiles() {
        return fromFiles;
    }

    public long clickCooldown() {
        return clickCooldown;
    }

    @Override
    public String toString() {
        return "MenuConfig{" +
                "id=" + id +
                ", supers=" + supers +
                '}';
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return id;
    }
}
