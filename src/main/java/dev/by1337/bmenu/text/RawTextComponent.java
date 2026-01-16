package dev.by1337.bmenu.text;

import dev.by1337.core.util.text.minimessage.MiniMessage;
import dev.by1337.plc.PlaceholderApplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.NonNull;

public class RawTextComponent implements SourcedComponentLike {
    private final String source;

    public RawTextComponent(String source) {
        this.source = source;
    }

    public @NonNull Component asComponent(PlaceholderApplier placeholders) {
        return MiniMessage.deserialize(placeholders.setPlaceholders(source)).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public @NonNull Component asComponent() {
        return MiniMessage.deserialize(source);
    }

    @Override
    public String source() {
        return source;
    }
}
