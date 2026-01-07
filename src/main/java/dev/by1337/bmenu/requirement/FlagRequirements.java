package dev.by1337.bmenu.requirement;

import dev.by1337.plc.Placeholderable;
import org.bukkit.entity.Player;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.menu.Menu;

import java.util.List;

public class FlagRequirements implements Requirement {
    private final boolean flag;
    private final Commands commands;
    private final Commands denyCommands;

    public FlagRequirements(boolean flag, Commands commands, Commands denyCommands) {
        this.flag = flag;
        this.commands = commands;
        this.denyCommands = denyCommands;
    }

    @Override
    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        return flag;
    }

    @Override
    public Commands getCommands() {
        return commands;
    }

    @Override
    public Commands getDenyCommands() {
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
