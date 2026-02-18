package dev.by1337.bmenu.yaml.codec;

import dev.by1337.yaml.YamlValue;

import java.util.function.Predicate;

public interface YamlTester extends Predicate<YamlValue> {
    boolean test(YamlValue v);

    YamlTester IF_MAP = YamlValue::isMap;
    YamlTester IF_LIST = YamlValue::isList;
    YamlTester IF_PRIMITIVE = YamlValue::isPrimitive;

    static YamlTester ifKey(String... keys) {
        return v -> {
            if (v.isMap()) {
                var map = v.asYamlMap().result();
                if (map == null) return false;
                for (String key : keys) {
                    if (map.has(key)) return true;
                }
            }
            return false;
        };
    }
}
