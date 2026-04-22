package dev.by1337.bmenu.requirement;

import dev.by1337.bmenu.command.PlayerContext;
import dev.by1337.bmenu.util.math.FastExpressionParser;
import dev.by1337.plc.PlaceholderApplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record MathRequirement(String input) implements Requirement {

    private static final Logger log = LoggerFactory.getLogger("BMenu");

    @Override
    public boolean test(PlayerContext ctx, PlaceholderApplier placeholders) {
        String s = placeholders.setPlaceholders(input);
        try {
            var v = FastExpressionParser.parse(s) == 1D;
            ctx.tracer().log("if '%s' -> %s", s, v);
            return v;
        } catch (FastExpressionParser.MathFormatException e) {
            log.error(
                    "Failed to parse math requirement. expression: '{}' replaced expression: '{}'\n{}",
                    input, s,
                    e.getMessage()
            );
            return false;
        }
    }

    public MathRequirement invert() {
        return new MathRequirement("(" + input + ") == 0");
    }

    public @Nullable Requirement compile() {
        //rnd, irnd
        if (input.contains("{") || input.contains("%") || input.contains("rnd")) return null;
        try {
            return FastExpressionParser.parse(input) == 1D ? Requirement.TRUE : Requirement.FALSE;
        } catch (FastExpressionParser.MathFormatException e) {
            return null;
        }
    }

    @Override
    public @NotNull String toString() {
        return input;
    }
}
