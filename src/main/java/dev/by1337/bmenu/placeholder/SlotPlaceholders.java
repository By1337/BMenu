package dev.by1337.bmenu.placeholder;

import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.util.map.HashMapLike;
import dev.by1337.plc.PlaceholderFormat;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.yaml.codec.YamlCodec;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SlotPlaceholders implements PlaceholderResolver<Menu> {
    public static final YamlCodec<SlotPlaceholders> CODEC = MenuCodecs.ARGS_CODEC
            .map(SlotPlaceholders::new, SlotPlaceholders::asStringMap);

    private final HashMapLike<String, Supplier<Object>> map;

    public SlotPlaceholders(Map<String, String> map) {
        this.map = new HashMapLike<>();
        map.forEach((k, v) -> this.map.put(k, () -> v));
    }

    public SlotPlaceholders(HashMapLike<String, Supplier<Object>> map) {
        this.map = map;
    }

    public SlotPlaceholders() {
        map = new HashMapLike<>();
    }

    public SlotPlaceholders copy() {
        return new SlotPlaceholders(map.copy());
    }

    public void set(String key, Supplier<Object> supplier) {
        map.put(key, supplier);
    }

    @Override
    public boolean has(String s, PlaceholderFormat placeholderFormat) {
        return PlaceholderFormat.BUKET == placeholderFormat && map.containsKey(s);
    }

    @Override
    public @Nullable String replace(String key, String params, @Nullable Menu ctx, PlaceholderFormat format) {
        if (format != PlaceholderFormat.BUKET) return null;
        var v = map.get(key);
        return v == null ? null : String.valueOf(v.get());
    }

    public Map<String, String> asStringMap() {
        Map<String, String> map = new HashMap<>();
        this.map.forEach((k, v) -> map.put(k, String.valueOf(v.get())));
        return map;
    }

}
