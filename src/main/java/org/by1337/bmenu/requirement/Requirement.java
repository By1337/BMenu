package org.by1337.bmenu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.bmenu.Menu;

import java.util.List;

public interface Requirement {
    boolean test(Menu menu, Placeholderable placeholderable, Player clicker);
    List<String> getCommands();
    List<String> getDenyCommands();
}
