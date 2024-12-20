package org.by1337.bmenu.yaml;

import org.by1337.blib.configuration.YamlValue;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CashedYamlContext extends RawYamlContext {

    private final Map<String, Object> cashed = new HashMap<>();

    public CashedYamlContext(RawYamlContext context) {
        super(context.getRaw());
    }


    public <T> T get(String path, Function<YamlValue, T> mapper) {
        return get(path, mapper, null);
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

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getFromCash(String path){
        return (T) cashed.get(path);
    }

    public void removeCash(String path) {
        cashed.remove(path);
    }

    public void setCash(String path, Object value) {
        cashed.put(path, value);
    }
}
