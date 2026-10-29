package dev.by1337.bmenu.slot.component;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.event.MenuEvents;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.edsl.EventHandler;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.YamlDecoder;
import org.jetbrains.annotations.Nullable;

public record OnViewComponent(@Nullable EventHandler handler) {
    public static final OnViewComponent EMPTY = new OnViewComponent((EventHandler) null);
    public static final YamlDecoder<OnViewComponent> DECODER = MenuEvents.CHECK_DECODER.map(OnViewComponent::new);
    @Deprecated
    public static YamlCodec<OnViewComponent> CODEC = YamlCodec.of(DECODER);

    @Deprecated
    public OnViewComponent(@Nullable Requirement requirement) {
        this(requirement == null ? null : MenuEvents.adapt(requirement));
    }

    public boolean isVisible(Menu menu, SlotContent slotContent) {
        if (handler == null) return true;
        try (var ctx = ExecuteContext.of(menu, slotContent, "on_view")) {
            return MenuEvents.test(handler, ctx, slotContent.getPlaceholders(menu));
        }
    }

    @Deprecated
    public @Nullable Requirement requirement() {
        return handler == null ? null : Requirement.of(handler);
    }

    public boolean isEmpty() {
        return handler == null;
    }
}
