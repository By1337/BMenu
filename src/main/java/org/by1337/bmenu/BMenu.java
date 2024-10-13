package org.by1337.bmenu;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.by1337.blib.BLib;
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
            ConfigUtil.trySave("menu/random-colors/rand-colors-menu.yml");
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
                )
/*                .addSubCommand(new Command<CommandSender>("item")
                        .requires(new RequiresPermission<>("bmenu.item"))
                        .addSubCommand(new Command<CommandSender>("dump")
                                .requires(new RequiresPermission<>("bmenu.item.dump"))
                                .requires(sender -> sender instanceof Player)
                                .executor((sender, args) -> {
                                   Player player = (Player) sender;
                                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                                    if (itemStack.getType().isAir()){
                                        loader.getMessage().sendMsg(sender, "&cУ Вас в руке должен быть предмет");
                                        return;
                                    }
                                    String item = BLib.getApi().getItemStackSerialize().serialize(itemStack);
                                    player.sendMessage(
                                            Component.text(item)
                                                    .hoverEvent(Component.text("click to copy"))
                                                    .clickEvent(ClickEvent.copyToClipboard(item))
                                    );
                                })
                        )
                )*/
                ;

        return cmd;
    }
}
