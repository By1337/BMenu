package dev.by1337.bmenu.text;

import dev.by1337.core.util.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SourcedComponent implements SourcedComponentLike {
    private final Component component;
    private final String source;

    public SourcedComponent(String source) {
        this.source = source;
        component = MiniMessage.deserialize(source).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public String source() {
        return source;
    }

    @Override
    public @NonNull Component asComponent() {
        return component;
    }
}
