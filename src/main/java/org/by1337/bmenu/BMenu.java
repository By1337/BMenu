package org.by1337.bmenu;


import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandSyntaxError;
import org.by1337.blib.command.CommandWrapper;
import org.by1337.blib.command.argument.ArgumentPlayer;
import org.by1337.blib.command.argument.ArgumentSetList;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.blib.util.SpacedNameKey;
import org.by1337.bmenu.util.ConfigUtil;

import java.io.File;

public class BMenu extends JavaPlugin {
    private MenuLoader loader;
    private CommandWrapper commandWrapper;
    private OpenCommands openCommands;

    @Override
    public void onLoad() {
        if (!new File(getDataFolder(), "menu").exists()){
            ConfigUtil.trySave("menu/animation-54.yml");
            ConfigUtil.trySave("menu/example-seller.yml");
            ConfigUtil.trySave("menu-shem.yml");
            ConfigUtil.trySave("menu/include-example/confirm.yml");
            ConfigUtil.trySave("menu/include-example/items.yml");
            ConfigUtil.trySave("menu/include-example/readme.txt");
            ConfigUtil.trySave("menu/include-example/seller.yml");
        }
        loader = new MenuLoader(
                new File(getDataFolder(), "menu"),
                this
        );
    }

    @Override
    public void onEnable() {
        openCommands = new OpenCommands(loader, ConfigUtil.load("config.yml"));
        loader.loadMenus();
        loader.registerListeners();
        commandWrapper = new CommandWrapper(createCommand(), this);
        commandWrapper.setPermission("bmenu.use");
        commandWrapper.register();
        openCommands.register();
    }

    @Override
    public void onDisable() {
        loader.close();
        commandWrapper.close();
        openCommands.unregister();
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
                            openCommands.unregister();
                            openCommands = new OpenCommands(loader, ConfigUtil.load("config.yml"));
                            openCommands.register();
                            loader.getMessage().sendMsg(sender, "&aReloaded {} menus!", loader.getMenuCount());
                        })
                )
                .addSubCommand(new Command<CommandSender>("open")
                        .requires(new RequiresPermission<>("bmenu.open"))
                        .argument(new ArgumentSetList<>("menu", () -> loader.getMenus().stream().map(SpacedNameKey::toString).toList()))
                        .argument(new ArgumentPlayer<>("player"))
                        .executor((sender, args) -> {
                            String menu = (String) args.getOrThrow("menu", "Use /bmenu open <menu> <player>");
                            Player player = (Player) args.get("player");
                            if (player == null) {
                                if (sender instanceof Player) {
                                    player = (Player) sender;
                                } else {
                                    throw new CommandSyntaxError("Use /bmenu open <menu> <player>");
                                }
                            }
                            Menu m = loader.findAndCreate(new SpacedNameKey(menu), player, null);
                            m.open();
                        })
                );

        return cmd;
    }
}
