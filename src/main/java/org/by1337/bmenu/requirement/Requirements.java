package org.by1337.bmenu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Requirements {
    public static final Requirements EMPTY = new Requirements(Collections.emptyList());
    private final List<Requirement> requirements;

    public Requirements(List<Requirement> requirements) {
        this.requirements = Collections.unmodifiableList(requirements);
    }


    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        boolean result = true;
        for (Requirement requirement : requirements) {
            try {
                if (!requirement.test(menu, placeholderable, clicker)) {
                    if (runCommands(requirement.getDenyCommands(), menu, placeholderable)){
                        return false;
                    }
                    result = false;
                } else {
                    if (runCommands(requirement.getCommands(), menu, placeholderable)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                menu.getLoader().getLogger().error("Failed to check requirement: {}", requirement, e);
            }
        }
        return result;
    }

    private boolean runCommands(List<String> commands, Menu menu, Placeholderable placeholderable) {
        for (String command : commands) {
            if (command.equals("[BREAK]")) return true;
            menu.runCommands(List.of(placeholderable.replace(command)));
        }
        return false;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }


    public static Requirements empty() {
        return EMPTY;
    }

}