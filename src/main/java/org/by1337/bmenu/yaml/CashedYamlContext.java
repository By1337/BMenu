package org.by1337.bmenu.yaml;

import org.by1337.blib.configuration.YamlValue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CashedYamlContext extends RawYamlContext {

    private final Map<String, Object> cashed = new HashMap<>();

    public CashedYamlContext(RawYamlContext context) {
        super(context.getRaw());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path, Function<YamlValue, T> mapper, T def) {
        if (cashed.containsKey(path)) {
            return (T) cashed.get(path);
        }
        YamlValue val = get(path);
        if (val.getValue() == null) {
            cashed.put(path, def);
            return def;
        }
        T result = mapper.apply(val);
        cashed.put(path, result);
        return result;
    }
}
