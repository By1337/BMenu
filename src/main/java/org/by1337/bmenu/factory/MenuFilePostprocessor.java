package org.by1337.bmenu.factory;

import org.bukkit.configuration.MemorySection;
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

        return new RawYamlContext((Map<String, Object>) process(raw, ""));
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
                to.put(key, from.get(key));
            } else {
                Object val = from.get(key);
                if (val instanceof Map<?, ?> map) {
                    Map<String, Object> to2 = (Map<String, Object>) to.computeIfAbsent(key, k -> new LinkedHashMap<>());
                    merge((Map<String, Object>) map, to2, false);
                }
            }
        }
    }
}
