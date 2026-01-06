package org.by1337.bmenu.click;


import dev.by1337.plc.Placeholderable;
import org.bukkit.entity.Player;
import org.by1337.bmenu.command.CommandRunner;
import org.by1337.bmenu.command.ExecuteContext;
import org.by1337.bmenu.menu.Menu;

@FunctionalInterface
public interface ClickHandler {

    void onClick(Menu menu, Placeholderable placeholders, Player player, ExecuteContext commandRunner);

}
