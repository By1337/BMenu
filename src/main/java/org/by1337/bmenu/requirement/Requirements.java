package org.by1337.bmenu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuLoader;

import java.util.Collections;
import java.util.List;

public class Requirements implements Requirement {
    public static final Requirements EMPTY = new Requirements(Collections.emptyList());
    private final List<Requirement> requirements;

    public Requirements(List<Requirement> requirements) {
        this.requirements = Collections.unmodifiableList(requirements);
    }

    @Override
    public boolean test(Menu menu, Placeholderable placeholderable, Player clicker) {
        for (Requirement requirement : requirements) {
            try {
                if (!requirement.test(menu, placeholderable, clicker)) {
                    return false;
                }
            } catch (Exception e) {
                menu.getLoader().getLogger().error("Failed to check requirement: {}", requirement, e);
            }
        }
        return true;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }


    public static Requirements empty() {
        return EMPTY;
    }
}