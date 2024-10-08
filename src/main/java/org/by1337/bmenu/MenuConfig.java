package org.by1337.bmenu;

import org.bukkit.event.inventory.InventoryType;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.util.SpacedNameKey;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MenuConfig {
    private final List<SpacedNameKey> supersId;
    private final List<MenuConfig> supers;
    private final @Nullable SpacedNameKey id;
    private final @Nullable SpacedNameKey provider;
    private final InventoryType invType;
    private final int size;
    private final List<SpacedNameKey> onlyOpenFrom;
    private final Map<String, String> args;
    private final Map<String, MenuItemBuilder> idToItems;
    private final YamlContext context;
    private final MenuLoader loader;
    private final String title;
    private final List<MenuItemBuilder> items;

    public MenuConfig(List<MenuConfig> supers, @Nullable SpacedNameKey id, @Nullable SpacedNameKey provider, InventoryType invType, int size, List<SpacedNameKey> onlyOpenFrom, Map<String, String> args, Map<String, MenuItemBuilder> idToItems, YamlContext context, MenuLoader loader, String title) {
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
        supersId = new ArrayList<>();
        items = idToItems.values().stream().sorted().toList();
        for (MenuConfig aSuper : supers) {
            if (aSuper.id != null){
                supersId.add(aSuper.id);
            }
        }
    }

    public void generate(Menu menu){
        for (MenuConfig aSuper : supers) {
            aSuper.generate(menu);
        }
        var currentItems = items.stream().map(m -> m.build(menu)).filter(Objects::nonNull).toList();
        menu.setItems(currentItems);
    }

    public YamlContext getContext() {
        return context;
    }

    public List<SpacedNameKey> getSupersId() {
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
}
