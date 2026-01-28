package dev.by1337.bmenu.util.holder;

import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.plc.PlaceholderApplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.OptionalInt;

public class IntHolder {
    public static final YamlCodec<IntHolder> CODEC = YamlCodec.STRING.map(
            IntHolder::new,
            IntHolder::src
    );
    public static final IntHolder ZERO = new IntHolder("0");
    public static final IntHolder ONE = new IntHolder("1");

    private static final Logger log = LoggerFactory.getLogger(IntHolder.class);
    private final String src;
    private int value;
    private boolean cashed;

    public IntHolder(String src) {
        this.src = src;
        if (hasNoPlaceholders(src)) {
            var v = tryGet(src);
            if (v.isPresent()) {
                value = v.getAsInt();
                cashed = true;
            }
        }
    }

    public int getOrDefault(PlaceholderApplier placeholder, int def) {
        if (cashed) return value;
        try {
            return Integer.parseInt(placeholder.setPlaceholders(src));
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

    private static boolean hasNoPlaceholders(String input) {
        return !input.contains("{") && !input.contains("%");
    }
}
