package dev.by1337.bmenu.util;

import dev.by1337.plc.PlaceholderFormat;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.factory.MenuCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MenuPlaceholders implements PlaceholderResolver<Menu> {
    public static final YamlCodec<MenuPlaceholders> CODEC = MenuCodecs.ARGS_CODEC.map(MenuPlaceholders::new, MenuPlaceholders::asStringMap);

    private HashMap<String, Supplier<Object>> args;
    private boolean copyOnWrite;

    public MenuPlaceholders(Map<String, String> map) {
        args = new HashMap<>();
        map.forEach((k, v) -> args.put(k, () -> v));
    }

    public MenuPlaceholders(HashMap<String, String> map) {
        args = new HashMap<>();
        map.forEach((k, v) -> args.put(k, () -> v));
    }

    public MenuPlaceholders(HashMap<String, Supplier<Object>> args, boolean copyOnWrite) {
        this.args = args;
        this.copyOnWrite = copyOnWrite;
    }
    public boolean isEmpty(){
        return args.isEmpty();
    }
    public static MenuPlaceholders empty(){
        return new MenuPlaceholders(new LinkedHashMap<>(), false);
    }

    public MenuPlaceholders copy() {
        return new MenuPlaceholders(args, true);
    }

    private void ensureMapOwnership() {
        if (this.copyOnWrite) {
            this.args = new LinkedHashMap<>(args);
            this.copyOnWrite = false;
        }
    }

    public void setPlaceholder(String placeholder, Supplier<Object> value) {
        ensureMapOwnership();
        args.put(placeholder, value);
    }

    @Nullable
    public Supplier<Object> getPlaceholder(String placeholder) {
        return args.get(placeholder);
    }

    public Map<String, String> asStringMap() {
        Map<String, String> map = new HashMap<>();
        args.forEach((k, v) -> map.put(k, String.valueOf(v.get())));
        return map;
    }

    @Override
    public boolean has(String key, PlaceholderFormat format) {
        return format == PlaceholderFormat.BUKET &&  args.containsKey(key);
    }

    @Override
    public @Nullable String replace(String key, String params, @Nullable Menu ctx, PlaceholderFormat format) {
        if (format != PlaceholderFormat.BUKET) return null;
        var v = args.get(key);
        return v == null ? null : String.valueOf(v.get());
    }

}
