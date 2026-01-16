package dev.by1337.bmenu.click;


import dev.by1337.plc.PlaceholderApplier;
import org.bukkit.entity.Player;
import dev.by1337.bmenu.command.CommandRunner;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.Menu;

@FunctionalInterface
public interface ClickHandler {

    void onClick(Menu menu, PlaceholderApplier placeholders, Player player, ExecuteContext commandRunner);

}
