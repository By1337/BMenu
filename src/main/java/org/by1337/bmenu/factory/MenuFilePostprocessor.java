package org.by1337.bmenu.factory;

import dev.by1337.cmd.CommandReader;
import dev.by1337.yaml.YamlMap;
import org.bukkit.configuration.MemorySection;

import java.util.*;

public class MenuFilePostprocessor {
    private static final String SOFT_MERGE_TAG = "<<+";
    private static final String HARD_MERGE_TAG = "<<*";


    public static YamlMap apply(String data) throws InvalidMenuConfigException {
        YamlMap yamlMap = YamlMap.loadFromString(data);
        fixPlaceholders(yamlMap.getRaw());
        process(yamlMap.getRaw(), "");
        return yamlMap;
    }

    //{rand_10} -> {rand:10}
    //{rand_100} -> {rand:100}
    //{rand_1000} -> {rand:1000}
    //{rand_10000} -> {rand:10000}
    //{rand_100000} -> {rand:100000}
    //math[(.*?)] -> {math:(.*?)}
    //${(.*?)} -> {(.*?)}
    private static Object fixPlaceholders(Object o) {
        if (o instanceof Map<?, ?> m) {
            m.entrySet().forEach(entry -> {
                Object value = entry.getValue();
                ((Map.Entry) entry).setValue(fixPlaceholders(value));
            });
            return o;
        } else if (o instanceof List<?>) {
            ((List<Object>) o).replaceAll(v -> fixPlaceholders(v));
            return o;
        } else if (o instanceof String s) {
            String fixed = s
                    .replace("{rand_10}", "{rand:10}")
                    .replace("{rand_100}", "{rand:100}")
                    .replace("{rand_1000}", "{rand:1000}")
                    .replace("{rand_10000}", "{rand:10000}")
                    .replace("{rand_100000}", "{rand:100000}")
                    .replaceAll("math\\[(.*?)]", "{math:$1}")
                    .replaceAll("\\$\\{(.*?)}", "{$1}");
            return fixCommands(fixed);
        } else {
            return o;
        }
    }

    //[set_item_to_layer] <slots> <layer> <item> -> [layer] <layer+2> [set] <item> <slots>
    //[clear_layer] <layer> -> [layer] <layer+2> [clear]
    private static String fixCommands(String src) {
        if (src.startsWith("[set_item_to_layer] ") || src.startsWith("[SET_ITEM_TO_LAYER] ")) {
            String args = src.substring("[set_item_to_layer] ".length());
            //<slots> <layer> <item>
            CommandReader reader = new CommandReader(args);
            String slots = reader.readString();
            reader.next();
            String layer = reader.readString();
            reader.next();
            String item = reader.readString();
            try {
                int x = Integer.parseInt(layer);
                return "[layer] " + (x + 2) + " [set] " + quoteAndEscape(item, false) + " " + quoteAndEscape(slots, false);
            } catch (NumberFormatException e) {
                return src;
            }
        } else if (src.startsWith("[clear_layer] ") || src.startsWith("[CLEAR_LAYER] ")) {
            String layer = src.substring("[clear_layer] ".length());
            try {
                int x = Integer.parseInt(layer);
                return "[layer] " + (x + 2) + " [clear]";
            } catch (NumberFormatException e) {
                return src;
            }

        }
        return src;
    }

    @SuppressWarnings("unchecked")
    private static Object process(Object o, String path) throws InvalidMenuConfigException {
        if (o instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) o;
            for (String key : new ArrayList<>(map.keySet())) {
                if (key.equals(SOFT_MERGE_TAG) || key.equals(HARD_MERGE_TAG)) {
                    Object from = process(map.get(key), path + "." + key);
                    if (from instanceof Map<?, ?> map1) {
                        map.remove(key);
                        merge((Map<String, Object>) map1, map, key.equals(HARD_MERGE_TAG));
                    } else if (from instanceof Collection<?> list) {
                        map.remove(key);
                        for (Object object : list) {
                            if (object instanceof Map<?, ?> map1) {
                                merge((Map<String, Object>) map1, map, key.equals(HARD_MERGE_TAG));
                            } else {
                                throw new InvalidMenuConfigException("Ожидалась мапа или список мап в {}.{}, а не {}", path, key, o);
                            }
                        }
                    } else {
                        throw new InvalidMenuConfigException("Ожидалась мапа или список мап в {}.{}, а не {}", path, key, o);
                    }
                } else {
                    map.put(key, process(map.get(key), path + "." + key));
                }
            }
        } else if (o instanceof Collection<?> collection) {
            List<Object> list = new ArrayList<>();
            for (Object object : collection) {
                list.add(process(object, path));
            }
            return list;
        }
        return o;
    }

    @SuppressWarnings("unchecked")
    private static void merge(Map<String, Object> from, Map<String, Object> to, boolean replace) {
        for (String key : from.keySet()) {
            if (!to.containsKey(key) || replace) {
                Object val = from.get(key);
                if (val instanceof Map<?, ?> map) {
                    Map<String, Object> to2 = (Map<String, Object>) to.computeIfAbsent(key, k -> new LinkedHashMap<>());
                    merge((Map<String, Object>) map, to2, false);
                } else {
                    to.put(key, deepCopy(val));
                }

            } else {
                Object val = from.get(key);
                if (val instanceof Map<?, ?> map) {
                    Map<String, Object> to2 = (Map<String, Object>) to.computeIfAbsent(key, k -> new LinkedHashMap<>());
                    merge((Map<String, Object>) map, to2, false);
                } else if (val instanceof Collection<?> collection) {
                    ((List<Object>) to.computeIfAbsent(key, k -> new ArrayList<>())).addAll((Collection<?>) deepCopy(collection));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Object deepCopy(Object object) {
        if (object instanceof Map) {
            Map<String, Object> originalMap = (Map<String, Object>) object;
            Map<String, Object> copiedMap = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
                copiedMap.put(entry.getKey(), deepCopy(entry.getValue()));
            }
            return copiedMap;
        } else if (object instanceof Collection<?> originalList) {
            List<Object> copiedList = new ArrayList<>();
            for (Object item : originalList) {
                copiedList.add(deepCopy(item));
            }
            return copiedList;
        } else {
            return object;
        }
    }

    public static String quoteAndEscape(String raw, boolean json) {
        StringBuilder result = new StringBuilder(" ");
        int quoteChar = 0;
        for (int i = 0; i < raw.length(); ++i) {
            char currentChar = raw.charAt(i);
            switch (currentChar) {
                case '\\':
                    result.append("\\\\");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                case '\b':
                    result.append("\\b");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\f':
                    result.append("\\f");
                    break;
                case '\"':
                case '\'':
                    if (quoteChar == 0) {
                        quoteChar = currentChar == '\"' ? '\'' : '\"';
                    }
                    if (json) {
                        quoteChar = '\"';
                    }
                    if (quoteChar == currentChar) {
                        result.append('\\');
                    }
                    result.append(currentChar);
                    break;
                default:
                    result.append(currentChar);
            }
        }
        if (quoteChar == 0) {
            quoteChar = '\"';
        }
        result.setCharAt(0, (char) quoteChar);
        result.append((char) quoteChar);
        return result.toString();
    }
}
