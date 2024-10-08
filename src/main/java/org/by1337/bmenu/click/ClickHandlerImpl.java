package org.by1337.bmenu.click;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.requirement.Requirement;

import java.util.List;

public class ClickHandlerImpl implements ClickHandler {
    private final List<String> denyCommands;
    private final List<String> commands;
    private final Requirement requirement;

    public ClickHandlerImpl(List<String> denyCommands, List<String> commands, Requirement requirement) {
        this.denyCommands = denyCommands;
        this.commands = commands;
        this.requirement = requirement;
    }

    @Override
    public void onClick(Menu menu, Placeholderable placeholderable, Player player) {
        if (requirement.test(menu, placeholderable, player)) {
            if (!commands.isEmpty())
                menu.runCommands(commands.stream().map(placeholderable::replace).toList());
        } else {
            if (!denyCommands.isEmpty())
                menu.runCommands(denyCommands.stream().map(placeholderable::replace).toList());
        }
    }
}
