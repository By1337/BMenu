package dev.by1337.bmenu.requirement;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.plc.PlaceholderApplier;
import org.jetbrains.annotations.NotNull;

public record PermissionRequirement(boolean not, String perm) implements  Requirement{

    @Override
    public boolean test(ExecuteContext ctx, PlaceholderApplier placeholders) {
        boolean state = ctx.menu.viewer().hasPermission(placeholders.setPlaceholders(perm));
        return not != state;
    }

    @Override
    public @NotNull String toString() {
        if (not) return "!has " + perm;
        return "has " + perm;
    }
}
