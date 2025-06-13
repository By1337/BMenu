package org.by1337.bmenu.util;

import dev.by1337.yaml.codec.YamlCodec;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.bmenu.factory.MenuCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MenuPlaceholders implements Placeholderable {
    public static final YamlCodec<MenuPlaceholders> CODEC = MenuCodecs.ARGS_CODEC.map(MenuPlaceholders::new, MenuPlaceholders::asStringMap);

    private LinkedHashMap<String, Supplier<Object>> args;
    private boolean copyOnWrite;

    public MenuPlaceholders(Map<String, String> map) {
        args = new LinkedHashMap<>();
        map.forEach((k, v) -> args.put(k, () -> v));
    }

    public MenuPlaceholders(LinkedHashMap<String, String> map) {
        args = new LinkedHashMap<>();
        map.forEach((k, v) -> args.put(k, () -> v));
    }

    public MenuPlaceholders(LinkedHashMap<String, Supplier<Object>> args, boolean copyOnWrite) {
        this.args = args;
        this.copyOnWrite = copyOnWrite;
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
    public String replace(String string) {
        StringBuilder sb = new StringBuilder(string);
        for (Map.Entry<String, Supplier<Object>> entry : args.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            int len = placeholder.length();
            int pos = sb.indexOf(placeholder);
            while (pos != -1) {
                var replaceTo = String.valueOf(entry.getValue().get());
                sb.replace(pos, pos + len, replaceTo);
                pos = sb.indexOf(placeholder, pos + replaceTo.length());
            }
        }
        return sb.toString();
    }
}
