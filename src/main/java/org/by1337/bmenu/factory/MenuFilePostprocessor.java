package org.by1337.bmenu.factory;

import com.google.common.base.Joiner;
import org.bukkit.configuration.MemorySection;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.bmenu.yaml.RawYamlContext;
import org.slf4j.Logger;

import java.util.*;

public class MenuFilePostprocessor {
    private static final String SOFT_MERGE_TAG = "<<+";
    private static final String HARD_MERGE_TAG = "<<*";


    @SuppressWarnings("unchecked")
    public static RawYamlContext apply(YamlContext context, Logger logger) throws InvalidMenuConfigException {
        Map<String, Object> raw = (Map<String, Object>) toMap(context.getHandle());

        RawYamlContext ctx = new RawYamlContext((Map<String, Object>) process(raw, ""));
        applyPlaceholders(ctx);
        return ctx;
    }

    @SuppressWarnings("unchecked")
    private static void applyPlaceholders(RawYamlContext ccontextx) {
        if (!ccontextx.has("items")) return;
        Map<String, Object> items = (Map<String, Object>) ccontextx.get("items").getValue();
        for (String string : items.keySet()) {
            Map<String, Object> item = (Map<String, Object>) items.get(string);
            if (!item.containsKey("args")) continue;

            Map<String, Object> args = (Map<String, Object>) deepCopy(item.get("args"));
            if (args.isEmpty()) continue;
            replaceLists(args);
            replacePlaceholdersInPlaceholders(args);
            Placeholder argsReplacer = new Placeholder();
            args.forEach((key, value) ->
                    argsReplacer.registerPlaceholder("${" + key + "}", () -> args.get(key))
            );

            items.put(string, replacePlaceholders(item, argsReplacer));
        }
    }

    private static void replaceLists(Map<String, Object> args) {
        for (String key : new ArrayList<>(args.keySet())) {
            Object item = args.get(key);
            if (item instanceof Collection<?> c) {
                args.put(key, Joiner.on("\\n").join(c));
            }
        }
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

    private static void replacePlaceholdersInPlaceholders(Map<String, Object> map) {
        List<String> args = new ArrayList<>(map.keySet());
        boolean hasChanges;
        do {
            hasChanges = false;
            for (String key : map.keySet()) {
                Object valueRaw = map.get(key);
                if (valueRaw instanceof String value) {
                    for (String arg : args) {
                        if (value.contains("${" + arg + "}")) {
                            map.put(key, value.replace("${" + arg + "}", String.valueOf(map.get(arg))));
                            hasChanges = true;
                        }
                    }
                }
            }
        } while (hasChanges);
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

    public static Map<?, ?> toMap(MemorySection memorySection) {
        Map<Object, Object> result = new LinkedHashMap<>();
        for (String key : memorySection.getKeys(false)) {
            Object value = memorySection.get(key);
            if (value instanceof MemorySection mem) {
                result.put(key, toMap(mem));
            } else if (value instanceof Collection<?> list) {
                List<Object> listToAdd = new ArrayList<>();
                for (Object o : list) {
                    if (o instanceof MemorySection mem) {
                        listToAdd.add(toMap(mem));
                    } else {
                        listToAdd.add(o);
                    }
                }
                result.put(key, listToAdd);
            } else {
                result.put(key, value);
            }
        }
        return result;
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
}
