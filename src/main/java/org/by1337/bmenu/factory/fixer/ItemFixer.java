package org.by1337.bmenu.factory.fixer;

import dev.by1337.plc.PlaceholderFormat;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.plc.Placeholders;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import org.by1337.bmenu.factory.MenuCodecs;
import org.by1337.bmenu.item.MenuItemTickListener;
import org.by1337.bmenu.util.math.FastExpressionParser;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ItemFixer {
    public static final String STATIC_LORE_TAG = "<static>";
    private static final YamlValue REPLACE_TICKING = MenuItemTickListener.CODEC.encode(MenuItemTickListener.DEFAULT);
    private static final Logger log = LoggerFactory.getLogger("BMenu");

    public static void fixItem(YamlMap map) {
        if (map.has("$fixed")) return;
        map.set("$fixed", true);
        rename(map, "tick-speed", "tick_speed");
        rename(map, "slots", "slot");
        rename(map, "display_name", "name");
        rename(map, "view_req", "view_requirement");

        replacePlaceholders(map);
        map.set("name", fixDisplay(map.getRaw("name")));
        map.set("lore", fixDisplay(map.getRaw("lore")));

        if (map.getRaw("static") == null &&
                hasNoPlaceholders(map.getRaw("name")) &&
                hasNoPlaceholders(map.getRaw("lore")) &&
                hasNoPlaceholders(map.getRaw("name")) &&
                hasNoPlaceholders(map.getRaw("damage")) &&
                hasNoPlaceholders(map.getRaw("amount"))
        ) {
            map.set("static", true);
            map.set("$synthetic-static", true);
        }
        if (Objects.equals(map.getRaw("ticking"), true)) {
            if (map.has("on_tick")) {
                map.set("$fixed-ticking", false);
                map.set("$fixed-ticking-failed", "item already has on_tick!");
            } else {
                map.set("$fixed-ticking", true);
                map.set("on_tick", REPLACE_TICKING.getValue());
            }
        }
    }

    private static void rename(YamlMap map, String from, String to) {
        if (map.has(from)) {
            map.getRaw().put(to, map.getRaw().remove(from));
            map.set("$rename-" + from, to);
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
        PlaceholderResolver<Void> placeholders = mapToResolver("args", item)
               // .and(mapToResolver("local_args", item))
                .and(new PlaceholderResolver<>() {
                    @Override
                    public boolean has(String key, PlaceholderFormat format) {
                        return key.equals("math");
                    }

                    @Override
                    public @Nullable String replace(String key, String params, @Nullable Void ctx, PlaceholderFormat format) {
                        if (params.contains("%") || params.contains("{"))
                            return null; // сейчас не известны не которые плейсы
                        try {
                            return String.valueOf(FastExpressionParser.parse(params));
                        } catch (FastExpressionParser.MathFormatException e) {
                            log.error(e.getMessage(), e);
                        }
                        return null;
                    }
                });

        Map<String, Object> copied = (Map<String, Object>) replacePlaceholders(item.getRaw(), placeholders);
        item.getRaw().clear();
        item.getRaw().putAll(copied);
    }

    private static PlaceholderResolver<Void> mapToResolver(String key, YamlMap map) {
        DataResult<Map<String, String>> decoded = map.get(key).decode(MenuCodecs.ARGS_CODEC);
        if (decoded.hasResult()) {
            var result = decoded.result();
            Placeholders<Void> placeholders = new Placeholders<>();
            result.forEach(placeholders::of);
            return placeholders;
        } else {
            return new Placeholders<>();
        }
    }

    @SuppressWarnings("unchecked")
    private static Object replacePlaceholders(Object in, PlaceholderResolver<Void> argsReplacer) {
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
            return argsReplacer.replace(s, null);
        } else {
            return in;
        }
    }
}
