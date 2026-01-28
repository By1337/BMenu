package dev.by1337.bmenu.slot.component;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.handler.MenuEventHandler;
import dev.by1337.bmenu.util.ObjectUtil;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public record ClickMapComponent(Map<MenuClickType, MenuEventHandler> map) {
    public static final ClickMapComponent EMPTY = new ClickMapComponent(Map.of());
    public static final YamlCodec<ClickMapComponent> CODEC = ObjectUtil.make(() -> {
        var builder = PipelineYamlCodecBuilder.of(() -> new EnumMap<MenuClickType, MenuEventHandler>(MenuClickType.class));
        for (MenuClickType value : MenuClickType.values()) {
            String key = value.getConfigKeyClick();
            builder.field(MenuEventHandler.CODEC, key,
                    m -> m.get(value),
                    (m, v) -> m.put(value, v)
            );
        }
        return builder
                .build()
                .map(
                        ClickMapComponent::new,
                        map -> new EnumMap<>(map.map)
                );

    });

    public void setClick(MenuClickType type, MenuEventHandler click) {
        map.put(type, click);
    }

    public @Nullable MenuEventHandler get(MenuClickType type) {
        return map.get(type);
    }

    public boolean doClick(MenuClickType type,ExecuteContext ctx, PlaceholderApplier placeholders) {
        MenuEventHandler handler = ObjectUtil.requireNonNullElseGet(map.get(type), () -> map.get(MenuClickType.ANY_CLICK));
        if (handler != null) {
            handler.run(ctx, placeholders);
            return true;
        }
        return false;
    }
}
