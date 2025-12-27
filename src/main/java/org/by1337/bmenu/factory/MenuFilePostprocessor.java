package org.by1337.bmenu.factory;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import org.bukkit.configuration.MemorySection;
import org.by1337.blib.configuration.YamlContext;

import java.util.*;
import java.util.stream.Collectors;

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
                ((Map.Entry)entry).setValue(fixPlaceholders(value));
            });
            return o;
        } else if (o instanceof List<?>) {
            ((List<Object>) o).replaceAll(v -> fixPlaceholders(v));
            return o;
        } else if (o instanceof String s) {
            return s
                    .replace("{rand_10}", "{rand:10}")
                    .replace("{rand_100}", "{rand:100}")
                    .replace("{rand_1000}", "{rand:1000}")
                    .replace("{rand_10000}", "{rand:10000}")
                    .replace("{rand_100000}", "{rand:100000}")
                    .replaceAll("math\\[(.*?)]", "{math:$1}")
                    .replaceAll("\\$\\{(.*?)}", "{$1}")
                    ;
        } else {
            return o;
        }
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

    @Deprecated
    public static YamlValue fromBLib(org.by1337.blib.configuration.YamlValue yamlValue) {
        Object raw = yamlValue.getValue();
        if (raw instanceof YamlContext ctx) {
            return YamlValue.wrap(toMap(ctx.getHandle()));
        } else if (raw instanceof MemorySection ms) {
            return YamlValue.wrap(toMap(ms));
        } else if (raw instanceof Collection<?> c) {
            List<Object> list = new ArrayList<>();
            for (Object o : c) {
                list.add(fromBLib(org.by1337.blib.configuration.YamlValue.wrap(o)).getValue());
            }
            return YamlValue.wrap(list);
        } else if (raw instanceof Map<?, ?>) {
            return YamlValue.wrap(
                    yamlValue.mapStream().collect(Collectors.toMap(
                            e -> fromBLib(e.getKey()).getValue(),
                            e -> fromBLib(e.getValue()).getValue(),
                            (v1, v2) -> v1,
                            LinkedHashMap::new
                    ))
            );
        }
        return YamlValue.wrap(raw);
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
