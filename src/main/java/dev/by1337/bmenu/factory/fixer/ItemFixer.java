package dev.by1337.bmenu.factory.fixer;

import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.factory.MenuFilePostprocessor;
import dev.by1337.bmenu.item.SlotTicker;
import dev.by1337.bmenu.util.math.FastExpressionParser;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.plc.PlaceholderSyntax;
import dev.by1337.plc.Placeholders;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ItemFixer {
    public static final String STATIC_LORE_TAG = "<static>";
    private static final YamlValue REPLACE_TICKING = SlotTicker.CODEC.encode(SlotTicker.DEFAULT);
    private static final Logger log = LoggerFactory.getLogger("BMenu");

    public static void fixItem(YamlMap map) {
        fixItem(map, null);
    }

    public static void fixItem(YamlMap map, @Nullable YamlMap superItem) {
        if (map.has("$fixed")) return;
        map.set("$fixed", true);
        rename(map, "tick-speed", "tick_speed");
        rename(map, "slots", "slot");
        rename(map, "display_name", "name");
        rename(map, "view_req", "view_requirement");
        rename(map, "view_requirement", "on_view");
        rename(map, "potion_effects", "potion_contents");

        replacePlaceholders(map, superItem);
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
        {// tick_speed -> on_tick.tick_speed
            Object tick_speed = map.getRaw("tick_speed");
            if (tick_speed != null) {
                map.getRaw().remove("tick_speed");
                map.set("$moved-tick_speed", tick_speed);
                Object on_tick = map.getRaw("on_tick");
                if (on_tick instanceof Map m) {
                    if (m.containsKey("tick_speed")) {
                        m.put("$moved-tick_speed", "Failed to move tick_speed -> on_tick.tick_speed");
                    } else {
                        m.put("tick_speed", tick_speed);
                    }
                } else {
                    map.set("$moved-tick_speed1", "on_tick not a map!");
                }
            }
        }


        //last
        if (map.has("oneOf")) {
            Object oneOf = MenuFilePostprocessor.deepCopy(map.getRaw("oneOf"));
            map.set("oneOf", oneOf);
            if (oneOf instanceof Map<?, ?> m) {
                for (Map.Entry<?, ?> entry : m.entrySet()) {
                    //после deepCopy всегда LinkedHashMap
                    if (entry.getValue() instanceof LinkedHashMap item) {
                        YamlMap sub = new YamlMap(item);
                        sub.set("$fix-oneOf", "true");
                        fixItem(sub, map);
                    }
                }
            } else if (oneOf instanceof Collection<?> collection) {
                for (Object o : collection) {
                    //после deepCopy всегда LinkedHashMap
                    if (o instanceof LinkedHashMap item) {
                        YamlMap sub = new YamlMap(item);
                        sub.set("$fix-oneOf", "true");
                        fixItem(sub, map);
                    }

                }
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
    private static void replacePlaceholders(YamlMap item, @Nullable YamlMap superItem) {
        PlaceholderResolver<Void> placeholders = mapToResolver("args", item)
                // .and(mapToResolver("local_args", item))
                .and(new PlaceholderResolver<>() {
                    @Override
                    public boolean has(String key, PlaceholderSyntax format) {
                        return key.equals("math");
                    }

                    @Override
                    public @Nullable String resolve(String key, String params, @Nullable Void ctx, PlaceholderSyntax format) {
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

        if (superItem != null) {
            placeholders = placeholders.and(mapToResolver("args", superItem));
        }

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
            return argsReplacer.setPlaceholders(s, null);
        } else {
            return in;
        }
    }
}
