package org.by1337.bmenu.click;


import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.bmenu.Menu;

@FunctionalInterface
public interface ClickHandler {
    void onClick(Menu menu, Placeholderable placeholderable, Player player);
}
