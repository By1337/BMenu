package dev.by1337.bmenu.util;

import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.plc.Placeholderable;

public class DataString {
    public static YamlCodec<DataString> CODEC = YamlCodec.STRING.map(
            DataString::new,
            DataString::src
    );
    private final String src;
    private String value;

    public DataString(String src) {
        this.src = src;
        if (StringUtil.hasNoPlaceholders(src)) {
            value = src;
        }
    }

    public String get(Placeholderable placeholderable) {
        if (value != null) return value;
        return placeholderable.replace(src);
    }

    public String src() {
        return src;
    }
}
