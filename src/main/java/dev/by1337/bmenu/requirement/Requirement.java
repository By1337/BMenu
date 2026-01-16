package dev.by1337.bmenu.requirement;

import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.plc.PlaceholderApplier;
import org.bukkit.entity.Player;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.menu.Menu;

import java.util.List;

public interface Requirement {
    boolean test(Menu menu, PlaceholderApplier placeholders, Player clicker);

    Commands getCommands();

    Commands getDenyCommands();

    default boolean compilable() {
        return false;
    }

    default boolean state() {
        return true;
    }

    default boolean isNOP() {
        return false;
    }

    default Requirement compile() {
        Requirement sub = this;
        return new Requirement() {
            private final boolean state = sub.state();
            @Override
            public boolean test(Menu menu, PlaceholderApplier placeholders, Player clicker) {
                return state;
            }

            @Override
            public Commands getCommands() {
                return sub.getCommands();
            }

            @Override
            public Commands getDenyCommands() {
                return sub.getDenyCommands();
            }

            @Override
            public boolean compilable() {
                return true;
            }

            @Override
            public boolean state() {
                return state;
            }

            @Override
            public Requirement compile() {
                return this;
            }

            @Override
            public boolean isNOP() {
                if (state) return getCommands().isEmpty();
                return getDenyCommands().isEmpty();
            }
        };
    }
}
