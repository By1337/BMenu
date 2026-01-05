package org.by1337.bmenu.click;


import dev.by1337.plc.PlaceholderResolver;
import org.bukkit.entity.Player;
import org.by1337.bmenu.CommandRunner;
import org.by1337.bmenu.menu.Menu;

@FunctionalInterface
public interface ClickHandler {

    void onClick(Menu menu, PlaceholderResolver<Menu> placeholders, Player player, CommandRunner commandRunner);

}
