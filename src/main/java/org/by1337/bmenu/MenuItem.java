package org.by1337.bmenu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.placeholder.BiPlaceholder;
import org.by1337.bmenu.click.ClickHandler;
import org.by1337.bmenu.click.MenuClickType;
import org.by1337.bmenu.item.MenuItemTickListener;
import org.by1337.bmenu.util.CachedSupplier;
import org.by1337.bmenu.util.MenuPlaceholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class MenuItem implements Placeholderable {
    private static final Logger log = LoggerFactory.getLogger("BMenu");
    private int[] slots;
    private CachedSupplier<MenuItem, ItemStack> itemStack;
    private Map<MenuClickType, ClickHandler> clicks;
    private @Nullable Placeholderable customPlaceholderable;
    private @Nullable Object data;
    private @Nullable MenuItemTickListener tickListener;
    private @NotNull Supplier<@Nullable MenuItem> builder;
    private int tickSpeed;
    private int ticks;
    private boolean doRebuild = false;
    private boolean die = false;
    private @Nullable MenuPlaceholders localArgs;

    public MenuItem(int[] slots, Function<MenuItem, ItemStack> itemStack, Map<MenuClickType, ClickHandler> clicks, @Nullable MenuItemTickListener tickListener, @Nullable Supplier<@Nullable MenuItem> builder, @Nullable MenuPlaceholders localArgs) {
        this.slots = slots;
        this.itemStack = new CachedSupplier<>(itemStack);
        this.clicks = clicks;
        this.tickListener = tickListener;
        this.builder = Objects.requireNonNullElse(builder, () -> this);
        if (localArgs != null) {
            this.localArgs = localArgs.copy();
        }
        if (tickListener != null) {
            if (this.localArgs == null) this.localArgs = new MenuPlaceholders(new LinkedHashMap<>(), false);
            this.localArgs.setPlaceholder("tick", () -> ticks / tickSpeed);
        }
    }

    @Deprecated
    public MenuItem(int[] slots, ItemStack itemStack, Map<MenuClickType, ClickHandler> clicks, boolean ticking, @Nullable Supplier<@Nullable MenuItem> builder) {
        this(
                slots,
                t -> itemStack,
                clicks,
                ticking ? MenuItemTickListener.DEFAULT : null,
                builder,
                null
        );
    }

    public MenuItem(int[] slots, ItemStack itemStack, Map<MenuClickType, ClickHandler> clicks) {
        this(slots, itemStack, clicks, false, () -> null);
    }

    public MenuItem(int[] slots, ItemStack itemStack) {
        this(slots, itemStack, Map.of());
    }

    public void executeCommand(String command, Menu menu) {
        if (command.equalsIgnoreCase("[rebuild]")) {
            doRebuild();
        } else if (command.equalsIgnoreCase("[die]")) {
            die();
        } else if (command.equalsIgnoreCase("[update]")) {
            invalidateCash();
        } else if (command.startsWith("[set_local] ") || command.startsWith("[SET_LOCAL] ")) {
            String[] args = command.split(" ", 3);
            if (args.length != 3){
                log.error("Failed to execute command! expected [set_local] <param> <value> but got {}", command);
                return;
            }
            setPlaceholder(args[1], () -> args[2]);
        } else {
            menu.executeCommand(command);
        }
    }

    public void doClick(Menu menu, Player player, MenuClickType type) {
        Placeholderable placeholderable = new BiPlaceholder(menu, this);
        ClickHandler handler = clicks.getOrDefault(type, clicks.get(MenuClickType.ANY_CLICK));
        if (handler != null) {
            handler.onClick(menu, placeholderable, player, s -> executeCommand(s, menu));
        }
    }

    public @NotNull Supplier<@Nullable MenuItem> getBuilder() {
        return builder;
    }

    public void setBuilder(@Nullable Supplier<@Nullable MenuItem> builder) {
        this.builder = Objects.requireNonNullElse(builder, () -> this);
    }

    public boolean isTicking() {
        return tickListener != null;
    }

    @Deprecated(forRemoval = true)
    public void setTicking(boolean ticking) {
        if (tickListener == null && ticking) {
            tickListener = MenuItemTickListener.DEFAULT;
        }
    }

    public int[] getSlots() {
        return slots;
    }

    public void setSlots(int[] slots) {
        this.slots = slots;
    }

    public ItemStack getItemStack() {
        return itemStack.apply(this);
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = new CachedSupplier<>(itemStack);
    }

    public Map<MenuClickType, ClickHandler> getClicks() {
        return Collections.unmodifiableMap(clicks);
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

    public void setCustomPlaceholderable(@Nullable Placeholderable customPlaceholderable) {
        this.customPlaceholderable = customPlaceholderable;
    }

    @Deprecated(forRemoval = true)
    public void doTick() {
    }

    public void doTick(Menu menu) {
        if (tickListener != null && ++ticks % tickSpeed == 0) {
            tickListener.tick(this, menu);
        }
    }

    public boolean shouldBeRebuild() {
        return doRebuild || die;
    }

    public int getTickSpeed() {
        return tickSpeed;
    }

    public void setTickSpeed(int tickSpeed) {
        this.tickSpeed = tickSpeed;
    }

    public void doRebuild() {
        doRebuild = true;
    }

    public void die() {
        die = true;
        builder = () -> null;
    }

    public void invalidateCash() {
        itemStack.invalidateCash();
    }

    @Override
    public String replace(String string) {
        var s = customPlaceholderable == null ? string : customPlaceholderable.replace(string);
        return localArgs == null ? s : localArgs.replace(s);
    }

    public void setPlaceholder(String placeholder, Supplier<Object> value) {
        if (localArgs == null) localArgs = new MenuPlaceholders(new LinkedHashMap<>(), false);
        localArgs.setPlaceholder(placeholder, value);
    }

    @Nullable
    public Supplier<Object> getPlaceholder(String placeholder) {
        if (localArgs == null) return null;
        return localArgs.getPlaceholder(placeholder);
    }

    public @Nullable MenuPlaceholders getLocalArgs() {
        return localArgs;
    }
}
