package dev.by1337.bmenu.slot.component;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.decoder.PipelineYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;
import dev.by1337.yaml.codec.YamlCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public record ClickMapComponent(Map<MenuClickType, Commands> map) {
    public static final ClickMapComponent EMPTY = new ClickMapComponent(Map.of());
    public static final YamlDecoder<ClickMapComponent> DECODER = make(() -> {
        var builder = PipelineYamlDecoder.of(() -> new EnumMap<MenuClickType, Commands>(MenuClickType.class));
        for (MenuClickType value : MenuClickType.values()) {
            builder.field(Commands.DECODER, value.getConfigKeyClick(), (m, v) -> m.put(value, v));
        }
        return builder.build().map(ClickMapComponent::new);
    });
    @Deprecated
    public static final YamlCodec<ClickMapComponent> CODEC = YamlCodec.of(DECODER);

    public void setClick(MenuClickType type, Commands click) {
        map.put(type, click);
    }

    public @Nullable Requirement get(MenuClickType type) {
        return map.get(type);
    }

    public boolean doClick(MenuClickType type, ExecuteContext ctx, PlaceholderApplier placeholders) {
        Requirement handler = requireNonNullElseGet(map.get(type), () -> {
            ctx.tracer.log("-> on_click");
            return map.get(MenuClickType.ANY_CLICK);
        });
        if (handler != null) {
            handler.test(ctx, placeholders);
            return true;
        }
        return false;
    }

    @Nullable
    @Contract(value = "!null, _ -> !null")
    public static <T> T requireNonNullElseGet(@Nullable T val, Supplier<@Nullable T> supplier) {
        return val != null ? val : supplier.get();
    }

    public static <T> T make(Supplier<T> maker) {
        return maker.get();
    }
}
