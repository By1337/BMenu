package org.by1337.bmenu.util;

import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.plc.Placeholderable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.OptionalInt;

public class DataInt {
    public static YamlCodec<DataInt> CODEC = YamlCodec.STRING.map(
            DataInt::new,
            DataInt::src
    );
    private static final Logger log = LoggerFactory.getLogger(DataInt.class);
    private final String src;
    private int value;
    private boolean cashed;

    public DataInt(String src) {
        this.src = src;
        if (StringUtil.hasNoPlaceholders(src)) {
            var v = tryGet(src);
            if (v.isPresent()) {
                value = v.getAsInt();
                cashed = true;
            }
        }
    }

    public int getOrDefault(Placeholderable placeholderable, int def) {
        if (cashed) return value;
        try {
            return Integer.parseInt(placeholderable.replace(src));
        } catch (NumberFormatException e) {
            log.error(e.getMessage());
        }
        return def;
    }

    private OptionalInt tryGet(String s) {
        try {
            return OptionalInt.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            log.error("Failed to parse int {}", s);
            return OptionalInt.empty();
        }
    }

    public String src() {
        return src;
    }
}
