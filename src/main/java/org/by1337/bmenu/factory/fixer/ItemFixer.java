package org.by1337.bmenu.factory.fixer;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.bmenu.factory.MenuFilePostprocessor;
import org.by1337.bmenu.item.MenuItemTickListener;
import org.by1337.bmenu.util.math.MathReplacer;

import java.util.*;

public class ItemFixer {
    public static final String STATIC_LORE_TAG = "<static>";
    private static final YamlValue REPLACE_TICKING = MenuItemTickListener.CODEC.encode(MenuItemTickListener.DEFAULT);

    public static void fixItem(YamlMap map) {
        if (map.has("$fixed")) return;
        map.setRaw("$fixed", true);
        rename(map, "tick-speed", "tick_speed");
        rename(map, "slots", "slot");
        rename(map, "display_name", "name");
        rename(map, "view_req", "view_requirement");

        replacePlaceholders(map);
        map.setRaw("name", fixDisplay(map.getRaw("name")));
        map.setRaw("lore", fixDisplay(map.getRaw("lore")));

        if (map.getRaw("static") == null &&
                hasNoPlaceholders(map.getRaw("name")) &&
                hasNoPlaceholders(map.getRaw("lore")) &&
                hasNoPlaceholders(map.getRaw("name")) &&
                hasNoPlaceholders(map.getRaw("damage")) &&
                hasNoPlaceholders(map.getRaw("amount"))
        ) {
            map.setRaw("static", true);
            map.setRaw("$synthetic-static", true);
        }
        if (Objects.equals(map.getRaw("ticking"), true)) {
            if (map.has("on_tick")) {
                map.setRaw("$fixed-ticking", false);
                map.setRaw("$fixed-ticking-failed", "item already has on_tick!");
            } else {
                map.setRaw("$fixed-ticking", true);
                map.set("on_tick", REPLACE_TICKING);
            }
        }
    }

    private static void rename(YamlMap map, String from, String to) {
        if (map.has(from)) {
            map.getRaw().put(to, map.getRaw().remove(from));
            map.setRaw("$rename-" + from, to);
        }
    }

    private static Object fixDisplay(Object o) {
        if (o == null) return null;
        if (o instanceof Collection<?> c) {
            List<String> result = new ArrayList<>();
            for (Object object : c) {
                String s = String.valueOf(object);
                if (s.contains("\n")) {
                    result.addAll(Arrays.asList(s.split("\n")));
                } else {
                    result.add(s);
                }
            }
            return result;
        }
        return o;
    }

    private static boolean hasNoPlaceholders(String s) {
        return !s.contains("{") && !s.contains("%");
    }

    private static boolean hasNoPlaceholders(Object o) {
        if (o instanceof String s) {
            return hasNoPlaceholders(s);
        } else if (o instanceof Collection<?> c) {
            for (Object object : c) {
                if (!hasNoPlaceholders(String.valueOf(object))) return false;
            }
            return true;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static void replacePlaceholders(YamlMap item) {
        if (!item.has("args")) return;
        Map<String, Object> rawArgs = (Map<String, Object>) MenuFilePostprocessor.deepCopy(item.getRaw("args"));

        Map<String, String> args = argsPostProcessor(rawArgs);
        Placeholder placeholder = new Placeholder();
        args.forEach((key, value) -> placeholder.registerPlaceholder("${" + key + "}", () -> value));
        Map<String, Object> result = (Map<String, Object>) replacePlaceholders(item.getRaw(), placeholder);
        item.getRaw().clear();
        item.getRaw().putAll(result);
        item.setRaw("args", args);
    }

    @SuppressWarnings("unchecked")
    private static Object replacePlaceholders(Object in, Placeholder argsReplacer) {
        if (in instanceof Map<?, ?> m) {
            Map<String, Object> map = (Map<String, Object>) m;
            Map<String, Object> result = new LinkedHashMap<>();
            for (String string : map.keySet()) {
                result.put(string, replacePlaceholders(map.get(string), argsReplacer));
            }
            return result;
        } else if (in instanceof Collection<?> c) {
            List<Object> result = new ArrayList<>();
            for (Object o : c) {
                result.add(replacePlaceholders(o, argsReplacer));
            }
            return result;
        } else if (in instanceof String s) {
            return argsReplacer.replace(s);
        } else {
            return in;
        }
    }

    private static Map<String, String> argsPostProcessor(Map<String, Object> rawArgs) {
        Map<String, String> args = new LinkedHashMap<>();
        Placeholder placeholder = new Placeholder();

        for (String key : rawArgs.keySet()) {
            String param = YamlCodec.MULTI_LINE_STRING.decode(YamlValue.wrap(rawArgs.get(key)));
            param = placeholder.replace(param);
            if (hasNoPlaceholders(param)) {
                param = MathReplacer.safeReplace(param);
            }
            args.put(key, param);
            final String finalParam = param;
            placeholder.registerPlaceholder("${" + key + "}", () -> finalParam);
        }
        return args;
    }
}
