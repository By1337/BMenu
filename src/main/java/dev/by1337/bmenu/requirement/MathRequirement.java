package dev.by1337.bmenu.requirement;

import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.util.math.FastExpressionParser;
import dev.by1337.plc.PlaceholderApplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MathRequirement(String input) implements Requirement {

    @Override
    public boolean test(Menu menu, PlaceholderApplier placeholders) {
        String s = placeholders.setPlaceholders(input);
        try {
            return FastExpressionParser.parse(s) == 1D;
        } catch (FastExpressionParser.MathFormatException e) {
            menu.loader().logger().error(
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
