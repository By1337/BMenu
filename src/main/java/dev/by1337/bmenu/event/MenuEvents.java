package dev.by1337.bmenu.event;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.command.PlayerContext;
import dev.by1337.bmenu.handler.MenuEventHandler;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.bmenu.compat.MenuEventDecoder;
import dev.by1337.edsl.EventHandler;
import dev.by1337.edsl.check.CheckHandler;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.decoder.YamlDecoder;

public final class MenuEvents {
    private static final MenuEventDecoder EVENTS = new MenuEventDecoder();
    public static final YamlDecoder<EventHandler> DECODER = EVENTS;
    public static final YamlDecoder<CheckHandler> CHECK_DECODER = EVENTS::decodeRequirement;
    public static final YamlDecoder<Requirement> REQUIREMENT_DECODER = (ctx, yaml) -> CHECK_DECODER.decode(ctx, yaml)
            .map(handler -> Requirement.of(handler, yaml));

    private MenuEvents() {
    }

    public static boolean test(EventHandler handler, PlayerContext player, PlaceholderApplier placeholders) {
        if (player instanceof ExecuteContext execution) {
            var event = execution.eventContext();
            // A legacy proxy may call back using this very EventContext as its applier.
            // Reinstalling it would make the live placeholder resolver call itself.
            if (placeholders == event) return handler.handle(event);
            try (var ignored = execution.withPlaceholders(placeholders)) {
                return handler.handle(event);
            }
        }
        return handler.handle(new MenuEventContext(player, placeholders));
    }

    public static EventHandler adapt(Requirement handler) {
        return event -> handler.test(event.get(PlayerContext.class), event);
    }

    public static EventHandler adapt(MenuEventHandler handler) {
        return adapt((Requirement) handler);
    }

    public static EventHandler input(MenuEventHandler handler) {
        return adapt(handler);
    }
}
