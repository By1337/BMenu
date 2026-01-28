package dev.by1337.bmenu.util.holder;

import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.plc.PlaceholderApplier;

public class StringHolder {
    public static YamlCodec<StringHolder> CODEC = YamlCodec.STRING.map(
            StringHolder::new,
            StringHolder::src
    );
    private final String src;
    private String value;

    public StringHolder(String src) {
        this.src = src;
        if (hasNoPlaceholders(src)) {
            value = src;
        }
    }

    public String get(PlaceholderApplier placeholder) {
        if (value != null) return value;
        return placeholder.setPlaceholders(src);
    }

    public String src() {
        return src;
    }
    private static boolean hasNoPlaceholders(String input) {
        return !input.contains("{") && !input.contains("%");
    }
}
