package org.by1337.bmenu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.bmenu.Menu;

import java.util.List;

public class FlagRequirements implements Requirement {
    private final boolean flag;
    private final List<String> commands;
    private final List<String> denyCommands;

    public FlagRequirements(boolean flag, List<String> commands, List<String> denyCommands) {
        this.flag = flag;
        this.commands = commands;
        this.denyCommands = denyCommands;
    }

    @Override
    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        return flag;
    }

    @Override
    public List<String> getCommands() {
        return commands;
    }

    @Override
    public List<String> getDenyCommands() {
        return denyCommands;
    }

    @Override
    public boolean compilable() {
        return true;
    }

    @Override
    public boolean state() {
        return flag;
    }
}
