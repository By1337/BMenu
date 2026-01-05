package org.by1337.bmenu.text;


import dev.by1337.core.util.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.by1337.bmenu.util.LazyLoad;
import org.checkerframework.checker.nullness.qual.NonNull;

public class JsonComponent implements SourcedComponentLike {
    private final String source;
    private final String json;
    private final LazyLoad<Component> component;

    public JsonComponent(String source) {
        this.source = source;
        this.json = GsonComponentSerializer.gson().serialize(MiniMessage.deserialize(source));
        component = new LazyLoad<>(() -> GsonComponentSerializer.gson().deserialize(json));
    }

    public String json() {
        return json;
    }

    public String source() {
        return source;
    }

    @Override
    public @NonNull Component asComponent() {
        return component.get();
    }
}
