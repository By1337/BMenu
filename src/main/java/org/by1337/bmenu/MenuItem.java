package org.by1337.bmenu;

import dev.by1337.cmd.Command;
import dev.by1337.cmd.CompiledCommand;
import dev.by1337.cmd.argument.ArgumentString;
import dev.by1337.plc.PlaceholderFormat;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.plc.Placeholderable;
import org.bukkit.entity.Player;
import org.by1337.bmenu.click.ClickHandler;
import org.by1337.bmenu.click.MenuClickType;
import org.by1337.bmenu.command.CommandRunner;
import org.by1337.bmenu.command.ExecuteContext;
import org.by1337.bmenu.item.ItemModel;
import org.by1337.bmenu.item.MenuItemTickListener;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.util.MenuPlaceholders;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MenuItem implements PlaceholderResolver<Menu> {
    private static final Logger log = LoggerFactory.getLogger("BMenu");
    private final ItemModel itemModel;
    private Map<MenuClickType, ClickHandler> clicks;
    private @Nullable Object data;
    private @Nullable MenuItemTickListener tickListener;
    private int tickSpeed;
    private int ticks;
    private boolean die = false;
    private boolean dirty;
    private @Nullable MenuPlaceholders localArgs;

    public MenuItem(ItemModel itemModel, Map<MenuClickType, ClickHandler> clicks, @Nullable MenuItemTickListener tickListener, @Nullable MenuPlaceholders localArgs) {
        this.itemModel = itemModel;

        this.clicks = clicks;
        this.tickListener = tickListener;
        if (localArgs != null) {
            this.localArgs = localArgs.copy();
        }
        if (tickListener != null) {
            if (this.localArgs == null) this.localArgs = new MenuPlaceholders(new HashMap<>(), false);
            this.localArgs.setPlaceholder("tick", () -> ticks / tickSpeed);
        }
    }
    public static MenuItem ofMaterial(String material){
        return new MenuItem(
                ItemModel.ofMaterial(material),
                Map.of(),
                null,
                null);
    }

    public Placeholderable getPlaceholders(Menu menu){
        return menu.getPlaceholderResolver().and(this).bind(menu);
    }

    public void doClick(Menu menu, Player player, MenuClickType type) {
        var resolver = menu.getPlaceholderResolver().and(this).bind(menu);
        ClickHandler handler = clicks.getOrDefault(type, clicks.get(MenuClickType.ANY_CLICK));
        if (handler != null) {
            handler.onClick(menu, resolver, player, ExecuteContext.of(menu, this));
        }
    }

    public boolean isTicking() {
        return tickListener != null;
    }

    public Map<MenuClickType, ClickHandler> getClicks() {
        return clicks;
    }

    public void setClicks(Map<MenuClickType, ClickHandler> clicks) {
        this.clicks = clicks;
    }

    public @Nullable Object getData() {
        return data;
    }

    public void setData(@Nullable Object data) {
        this.data = data;
    }


    public void doTick(Menu menu) {
        if (tickListener != null && ++ticks % tickSpeed == 0) {
            tickListener.tick(this, menu);
        }
    }

    public int getTickSpeed() {
        return tickSpeed;
    }

    public void setTickSpeed(int tickSpeed) {
        this.tickSpeed = tickSpeed;
    }

    public void die() {
        die = true;
    }

    public boolean isDie() {
        return die;
    }

    public void setDie(boolean die) {
        this.die = die;
    }

    public void setPlaceholder(String placeholder, Supplier<Object> value) {
        if (localArgs == null) localArgs = new MenuPlaceholders(new LinkedHashMap<>(), false);
        localArgs.setPlaceholder(placeholder, value);
        dirty = true;
    }

    public @Nullable MenuPlaceholders getLocalArgs() {
        return localArgs;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public ItemModel getItemModel() {
        return itemModel;
    }

    @Override
    public boolean has(String key, PlaceholderFormat format) {
        return localArgs != null && localArgs.has(key, format);
    }

    @Override
    public @Nullable String replace(String key, String params, @Nullable Menu ctx, PlaceholderFormat format) {
        return localArgs != null ? localArgs.replace(key, params, ctx, format) : null;
    }
}
