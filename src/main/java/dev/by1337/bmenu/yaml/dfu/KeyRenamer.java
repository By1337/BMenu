package dev.by1337.bmenu.yaml.dfu;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;

import java.util.Map;
import java.util.function.Function;

public class KeyRenamer implements Function<YamlValue, YamlValue> {
    private final Map<String, String> map;

    public KeyRenamer(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public YamlValue apply(YamlValue v) {
        var res = v.asYamlMap();
        var src = res.result();
        if (src != null) {
            map.forEach((oldKey, newKey) -> rename(src, oldKey, newKey));
            return YamlValue.wrap(src);
        }
        return v;
    }
    private static void rename(YamlMap map, String from, String to) {
        if (map.has(from)) {
            map.getRaw().put(to, map.getRaw().remove(from));
            map.set("$rename-" + from, to);
        }
    }
}
