package org.by1337.bmenu;


import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandWrapper;
import org.by1337.blib.command.argument.ArgumentPlayer;
import org.by1337.blib.command.argument.ArgumentSetList;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.blib.util.SpacedNameKey;
import org.by1337.bmenu.requirement.HasPermisionRequirement;
import org.by1337.bmenu.util.ConfigUtil;

import java.io.File;

public class BMenu extends JavaPlugin {
    private MenuLoader loader;
    private CommandWrapper commandWrapper;

    @Override
    public void onLoad() {
        loader = new MenuLoader(
                new File(getDataFolder(), "menu"),
                this
        );
        ConfigUtil.trySave("menu/animation-54.yml");
        ConfigUtil.trySave("menu/example-seller.yml");
    }

    @Override
    public void onEnable() {
        loader.loadMenus();
        loader.registerListeners();
        commandWrapper = new CommandWrapper(createCommand(), this);
        commandWrapper.setPermission("bmenu.use");
        commandWrapper.register();
    }

    @Override
    public void onDisable() {
        loader.close();
        commandWrapper.close();
    }

    private Command<CommandSender> createCommand() {
        Command<CommandSender> cmd = new Command<CommandSender>("bmenu")
                .aliases("bm")
                .requires(new RequiresPermission<>("bmenu.use"))
                .addSubCommand(new Command<CommandSender>("reload")
                        .requires(new RequiresPermission<>("bmenu.reload"))
                        .executor((sender, args) -> {
                            loader.close();
                            loader = new MenuLoader(
                                    new File(getDataFolder(), "menu"),
                                    this
                            );
                            loader.loadMenus();
                            loader.registerListeners();
                            loader.getMessage().sendMsg(sender, "&aReloaded {} menus!", loader.getMenuCount());
                        })
                )
                .addSubCommand(new Command<CommandSender>("open")
                        .requires(new RequiresPermission<>("bmenu.open"))
                        .argument(new ArgumentSetList<>("menu", () -> loader.getMenus().stream().map(SpacedNameKey::toString).toList()))
                        .argument(new ArgumentPlayer<>("player"))
                        .executor((sender, args) -> {
                            String menu = (String) args.getOrThrow("menu", "Use /bmenu open <menu> <player>");
                            Player player = (Player) args.getOrThrow("player", "Use /bmenu open <menu> <player>");
                            Menu m = loader.findAndCreate(new SpacedNameKey(menu), player, null);
                            m.open();
                        })
                );

        return cmd;
    }
}
