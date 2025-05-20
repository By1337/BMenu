package org.by1337.bmenu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.placeholder.BiPlaceholder;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.bmenu.click.ClickHandler;
import org.by1337.bmenu.click.MenuClickType;
import org.by1337.bmenu.item.MenuItemTickListener;
import org.by1337.bmenu.util.CachedSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class MenuItem extends Placeholder {
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
    private boolean recreate = false;

    public MenuItem(int[] slots, Function<MenuItem, ItemStack> itemStack, Map<MenuClickType, ClickHandler> clicks, @Nullable MenuItemTickListener tickListener, @Nullable Supplier<@Nullable MenuItem> builder) {
        this.slots = slots;
        this.itemStack = new CachedSupplier<>(itemStack);
        this.clicks = clicks;
        this.tickListener = tickListener;
        this.builder = Objects.requireNonNullElse(builder, () -> this);
        registerPlaceholder("{tick}", () -> ticks / tickSpeed);
        customPlaceholderable = this;
    }

    public MenuItem(int[] slots, ItemStack itemStack, Map<MenuClickType, ClickHandler> clicks, boolean ticking, @Nullable Supplier<@Nullable MenuItem> builder) {
        this(
                slots,
                t -> itemStack,
                clicks,
                ticking ? MenuItemTickListener.DEFAULT : null,
                builder
        );
    }

    public MenuItem(int[] slots, ItemStack itemStack, Map<MenuClickType, ClickHandler> clicks) {
        this(slots, itemStack, clicks, false, () -> null);
    }

    public MenuItem(int[] slots, ItemStack itemStack) {
        this(slots, itemStack, Map.of());
    }

    public void doClick(Menu menu, Player player, MenuClickType type) {
        Placeholderable placeholderable;
        if (customPlaceholderable == null) {
            placeholderable = menu;
        } else {
            placeholderable = new BiPlaceholder(menu, customPlaceholderable);
        }

        ClickHandler handler = clicks.get(type);
        if (handler != null) {
            handler.onClick(menu, placeholderable, player);
        } else {
            handler = clicks.get(MenuClickType.ANY_CLICK);
            if (handler != null)
                handler.onClick(menu, placeholderable, player);
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
        if (customPlaceholderable == null)
            this.customPlaceholderable = this;
        else this.customPlaceholderable = new BiPlaceholder(this, customPlaceholderable);
    }

    @Deprecated(forRemoval = true)
    public void doTick() {
    }

    public void doTick(Menu menu) {
        if (tickListener != null && ++ticks % tickSpeed == 0) {
            tickListener.tick(this, menu, ticks / tickSpeed);
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
}
