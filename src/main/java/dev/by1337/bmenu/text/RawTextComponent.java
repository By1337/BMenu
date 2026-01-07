package dev.by1337.bmenu.text;

import dev.by1337.core.util.text.minimessage.MiniMessage;
import dev.by1337.plc.Placeholderable;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class RawTextComponent implements SourcedComponentLike {
    private final String source;

    public RawTextComponent(String source) {
        this.source = source;
    }

    public @NonNull Component asComponent(Placeholderable placeholders) {
        return MiniMessage.deserialize(placeholders.replace(source));
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
