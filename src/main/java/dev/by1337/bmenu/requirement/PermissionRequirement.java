package dev.by1337.bmenu.requirement;

import dev.by1337.bmenu.menu.Menu;
import dev.by1337.plc.PlaceholderApplier;
import org.jetbrains.annotations.NotNull;

public record PermissionRequirement(Operation op, String perm) implements  Requirement{

    @Override
    public boolean test(Menu menu, PlaceholderApplier placeholders) {
        boolean state = menu.viewer().hasPermission(placeholders.setPlaceholders(perm));
        return op.state == state;
    }

    @Override
    public @NotNull String toString() {
        return op.name + " " + perm;
    }

    public enum Operation{
        YES("has", true),
        NO("!has", false)
        ;
        private final String name;
        private final boolean state;

        Operation(String name, boolean state) {
            this.name = name;
            this.state = state;
        }
    }
}
