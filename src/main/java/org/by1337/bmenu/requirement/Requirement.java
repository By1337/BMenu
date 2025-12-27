package org.by1337.bmenu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.bmenu.Menu;

import java.util.List;

public interface Requirement {
    boolean test(Menu menu, Placeholderable placeholderable, Player clicker);

    List<String> getCommands();

    List<String> getDenyCommands();

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
            public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
                return state;
            }

            @Override
            public List<String> getCommands() {
                return sub.getCommands();
            }

            @Override
            public List<String> getDenyCommands() {
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
