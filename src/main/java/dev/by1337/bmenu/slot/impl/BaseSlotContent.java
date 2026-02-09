package dev.by1337.bmenu.slot.impl;

import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.placeholder.PlaceholderResolverList;
import dev.by1337.bmenu.placeholder.SimplePlaceholders;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.plc.PlaceholderSyntax;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class BaseSlotContent implements SlotContent {
    private final SimplePlaceholders localArgs;
    private Object value;
    private boolean removed = false;
    protected boolean dirty = true;
    protected @Nullable PlaceholderResolverList custom;

    public BaseSlotContent() {
        this.localArgs = new SimplePlaceholders();
    }

    public BaseSlotContent(SimplePlaceholders localArgs) {
        this.localArgs = localArgs;
    }

    public void addCustomResolver(@Nullable PlaceholderResolver<Menu> custom) {
        if (custom == null) return;
        if (this.custom == null) this.custom = new PlaceholderResolverList();
        this.custom.addResolver(custom);
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
    public PlaceholderApplier getPlaceholders(Menu menu) {
        return menu.resolvers().and(this).bind(menu);
    }

    public void setPlaceholder(String placeholder, Supplier<Object> value) {
        localArgs.set(placeholder, value);
        dirty = true;
    }

    @Override
    public boolean has(String key, PlaceholderSyntax format) {
        return localArgs.has(key, format) || (custom != null && custom.has(key, format));
    }

    @Override
    public @Nullable String resolve(String key, String params, @Nullable Menu ctx, PlaceholderSyntax format) {
        var v = localArgs.resolve(key, params, ctx, format);
        if (v == null && custom != null) return custom.resolve(key, params, ctx, format);
        return v;
    }
}
