package dev.by1337.bmenu.command;

import dev.by1337.bmenu.compat.MenuEventDecoder;
import dev.by1337.bmenu.event.MenuEventContext;
import dev.by1337.bmenu.event.MenuEvents;
import dev.by1337.bmenu.handler.MenuEventHandler;
import dev.by1337.edsl.EventHandler;
import dev.by1337.edsl.handle.ListHandler;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.YamlDecoder;

import java.util.ArrayList;
import java.util.List;

/** Public BMenu API facade; event execution is owned by EventDSL. */
public class Commands implements MenuEventHandler {
    public static final Commands EMPTY = new Commands(List.of());
    public static final YamlDecoder<Commands> DECODER = (ctx, yaml) -> MenuEvents.DECODER.decode(ctx, yaml)
            .map(handler -> new Commands(handler, yaml));
    @Deprecated
    public static final YamlCodec<Commands> CODEC = YamlCodec.of(DECODER);

    private final List<EventHandler> handlers = new ArrayList<>();
    private final List<Object> sources = new ArrayList<>();
    private boolean hasBreak;

    private Commands(EventHandler handler, YamlValue source) {
        if (!MenuEventDecoder.isEmpty(handler)) handlers.add(handler);
        hasBreak = MenuEventDecoder.hasBreak(handler);
        sources.add(source.getRaw());
    }

    public Commands(List<MenuEventHandler> commands, boolean hasBreak) {
        commands.forEach(this::addHandler);
        this.hasBreak = hasBreak;
    }

    public Commands(List<String> list) {
        if (!list.isEmpty()) {
            Commands decoded = DECODER.decode(YamlValue.wrap(list)).getOrThrow();
            handlers.addAll(decoded.handlers);
            sources.addAll(decoded.sources);
            hasBreak = decoded.hasBreak;
        }
    }

    @Override
    public boolean test(PlayerContext ctx, PlaceholderApplier placeholders) {
        return MenuEvents.test(this::handle, ctx, placeholders);
    }

    public boolean handle(dev.by1337.edsl.context.EventContext ctx) {
        return new ListHandler(handlers).handle(ctx);
    }

    public static Commands fromCommandsList(List<Commands> list) {
        Commands result = new Commands(List.of());
        for (Commands commands : list) {
            result.handlers.addAll(commands.handlers);
            result.sources.addAll(commands.sources);
            if (commands.hasBreak) {
                result.hasBreak = true;
                break;
            }
        }
        return result;
    }

    public boolean isHasBreak() {
        return hasBreak;
    }

    public boolean isEmpty() {
        return handlers.isEmpty();
    }

    @Override
    public YamlValue encode() {
        List<Object> values = new ArrayList<>();
        for (Object source : sources) {
            if (source instanceof List<?> list) values.addAll(list);
            else values.add(source);
        }
        return YamlValue.wrap(values);
    }

    public void addHandler(MenuEventHandler handler) {
        handlers.add(MenuEvents.adapt(handler));
        sources.add(handler.encode().getRaw());
    }

    @Override
    public String toString() {
        return "Commands{" + sources + ", hasBreak=" + hasBreak + '}';
    }
}
