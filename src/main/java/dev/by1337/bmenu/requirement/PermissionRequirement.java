package dev.by1337.bmenu.requirement;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.plc.PlaceholderApplier;
import org.jetbrains.annotations.NotNull;

public record PermissionRequirement(boolean not, String perm) implements Requirement {

    @Override
    public boolean test(ExecuteContext ctx, PlaceholderApplier placeholders) {
        String s = placeholders.setPlaceholders(perm);
        boolean state = ctx.menu.viewer().hasPermission(s);
        var v = not != state;
        if (not) {
            ctx.tracer.log("if '!has %s' -> %s", s, v);
        } else {
            ctx.tracer.log("if 'has %s' -> %s", s, v);
        }
        return v;
    }

    @Override
    public @NotNull String toString() {
        if (not) return "!has " + perm;
        return "has " + perm;
    }
}
