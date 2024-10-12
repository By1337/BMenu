package org.by1337.bmenu;

import org.bukkit.event.inventory.InventoryType;
import org.by1337.blib.util.SpacedNameKey;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.command.CommandList;
import org.by1337.bmenu.requirement.CommandRequirements;
import org.by1337.bmenu.yaml.RawYamlContext;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MenuConfig implements MenuItemLookup {
    private final Set<SpacedNameKey> supersId;
    private final List<MenuConfig> supers;
    private final @Nullable SpacedNameKey id;
    private final @Nullable SpacedNameKey provider;
    private final InventoryType invType;
    private final int size;
    private final List<SpacedNameKey> onlyOpenFrom;
    private final Map<String, String> args;
    private final Map<String, MenuItemBuilder> idToItems;
    private final RawYamlContext context;
    private final MenuLoader loader;
    private final String title;
    private final List<MenuItemBuilder> items;
    private @Nullable Animator.AnimatorContext animation;
    private final CommandList commandList;
    private final Map<String, CommandRequirements> menuEventListeners;

    public MenuConfig(List<MenuConfig> supers, @Nullable SpacedNameKey id, @Nullable SpacedNameKey provider, InventoryType invType, int size, List<SpacedNameKey> onlyOpenFrom, Map<String, String> args, Map<String, MenuItemBuilder> idToItems, RawYamlContext context, MenuLoader loader, String title, @Nullable Animator.AnimatorContext animation, CommandList commandList, Map<String, CommandRequirements> menuEventListeners) {
        this.supers = supers;
        this.id = id;
        this.provider = provider;
        this.invType = invType;
        this.size = size;
        this.onlyOpenFrom = onlyOpenFrom;
        this.args = args;
        this.idToItems = idToItems;
        this.context = context;
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
        }
    }

    public boolean canOpenFrom(@Nullable Menu menu) {
        if (onlyOpenFrom.isEmpty()) return true;
        if (menu == null || menu.getConfig().id == null) return false;
        if (onlyOpenFrom.contains(menu.getConfig().id)) return true;
        for (SpacedNameKey spacedNameKey : menu.getConfig().supersId) {
            if (supersId.contains(spacedNameKey)) return true;
        }
        return false;
    }

    public void generate(Menu menu) {
        for (MenuConfig superMenu : supers) {
            superMenu.generate(menu);
        }
        var currentItems = items.stream().map(m -> m.build(menu)).filter(Objects::nonNull).toList();
        menu.setItems(currentItems);
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

    public Map<String, CommandRequirements> getMenuEventListeners() {
        return menuEventListeners;
    }

    public RawYamlContext getContext() {
        return context;
    }

    public Set<SpacedNameKey> getSupersId() {
        return supersId;
    }

    public List<MenuConfig> getSupers() {
        return supers;
    }

    public @Nullable SpacedNameKey getId() {
        return id;
    }

    public @Nullable SpacedNameKey getProvider() {
        return provider;
    }

    public InventoryType getInvType() {
        return invType;
    }

    public int getSize() {
        return size;
    }

    public List<SpacedNameKey> getOnlyOpenFrom() {
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
}
