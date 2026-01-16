package dev.by1337.bmenu.click;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.item.SlotContent;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.util.ObjectUtil;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public record ClickMap(Map<MenuClickType, ClickHandler> map) {
    public static final ClickMap EMPTY = new ClickMap(Map.of());
    public static final YamlCodec<ClickMap> CODEC = ObjectUtil.make(() -> {
        var builder = PipelineYamlCodecBuilder.of(() -> new EnumMap<MenuClickType, ClickHandler>(MenuClickType.class));
        for (MenuClickType value : MenuClickType.values()) {
            String key = value.getConfigKeyClick();
            builder.field(ClickHandlerImpl.CODEC, key,
                    m -> {
                        if (m.get(value) instanceof ClickHandlerImpl impl) return impl;
                        return null;
                    },
                    (m, v) -> m.put(value, v)
            );
        }
        return builder
                .build()
                .map(
                        ClickMap::new,
                        map -> new EnumMap<>(map.map)
                );

    });

    public void setClick(MenuClickType type, ClickHandler click) {
        map.put(type, click);
    }

    public @Nullable ClickHandler get(MenuClickType type) {
        return map.get(type);
    }

    public boolean doClick(Menu menu, Player player, MenuClickType type, SlotContent item) {
        return doClick(menu, player, type, item.getPlaceholders(menu), item);
    }
    public boolean doClick(Menu menu, Player player, MenuClickType type, PlaceholderApplier placeholders, SlotContent item) {
        ClickHandler handler = ObjectUtil.requireNonNullElseGet(map.get(type), () -> map.get(MenuClickType.ANY_CLICK));
        if (handler != null) {
            handler.onClick(menu, placeholders, player, ExecuteContext.of(menu, item));
            return true;
        }
        return false;
    }
}
