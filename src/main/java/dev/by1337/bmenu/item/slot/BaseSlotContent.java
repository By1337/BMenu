package dev.by1337.bmenu.item.slot;

import dev.by1337.bmenu.item.SlotContent;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.placeholder.SlotPlaceholders;
import dev.by1337.plc.PlaceholderFormat;
import dev.by1337.plc.Placeholderable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class BaseSlotContent implements SlotContent {
    private final SlotPlaceholders localArgs;
    private Object value;
    private boolean removed = false;
    protected boolean dirty = true;

    public BaseSlotContent() {
        this.localArgs = new SlotPlaceholders();
    }

    public BaseSlotContent(SlotPlaceholders localArgs) {
        this.localArgs = localArgs;
    }

    @Override
    public @Nullable Object getPayload() {
        return value;
    }

    @Override
    public void setPayload(@Nullable Object data) {
        value = data;
    }

    @Override
    public void markRemoved() {
        removed = true;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public boolean isVisible(Menu menu) {
        return true;
    }

    @Override
    public Placeholderable getPlaceholders(Menu menu) {
        return menu.getPlaceholderResolver().and(this).bind(menu);
    }

    public void setPlaceholder(String placeholder, Supplier<Object> value) {
        localArgs.set(placeholder, value);
        dirty = true;
    }

    @Override
    public boolean has(String key, PlaceholderFormat format) {
        return localArgs.has(key, format);
    }

    @Override
    public @Nullable String replace(String key, String params, @Nullable Menu ctx, PlaceholderFormat format) {
        return localArgs.replace(key, params, ctx, format);
    }
}
