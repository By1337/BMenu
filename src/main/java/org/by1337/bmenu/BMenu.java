package org.by1337.bmenu;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.by1337.blib.util.SpacedNameKey;
import org.by1337.bmenu.util.ConfigUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class BMenu extends JavaPlugin {
    private MenuLoader loader;

    @Override
    public void onLoad() {
        loader = new MenuLoader(
                new File(getDataFolder(), "menu"),
                this
        );
        ConfigUtil.trySave("menu/animation-54.yml");
        ConfigUtil.trySave("menu/example-buyer.yml");
    }

    @Override
    public void onEnable() {
        loader.loadMenus();
        loader.registerListeners();
    }

    @Override
    public void onDisable() {
        loader.close();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        Menu menu = loader.findAndCreate(new SpacedNameKey("example", "buyer"), player, null);
        menu.open();
        return true;
    }
}
