package org.by1337.bmenu;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandSyntaxError;
import org.by1337.blib.command.CommandWrapper;
import org.by1337.blib.command.argument.ArgumentPlayer;
import org.by1337.blib.command.argument.ArgumentSetList;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.blib.util.ResourceUtil;
import org.by1337.blib.util.SpacedNameKey;
import org.by1337.bmenu.metrics.Metrics;
import org.by1337.bmenu.network.BungeeCordMessageSender;

import java.io.File;

public class BMenu extends JavaPlugin {
    private MenuLoader loader;
    private CommandWrapper commandWrapper;
    private OpenCommands openCommands;
    private Metrics metrics;

    @Override
    public void onLoad() {
        if (!new File(getDataFolder(), "menu").exists()) {
            ResourceUtil.saveIfNotExist("menu/animation-54.yml", this);
            ResourceUtil.saveIfNotExist("menu/example-seller.yml", this);
            ResourceUtil.saveIfNotExist("menu-shem.yml", this);
            ResourceUtil.saveIfNotExist("menu/include-example/confirm.yml", this);
            ResourceUtil.saveIfNotExist("menu/include-example/items.yml", this);
            ResourceUtil.saveIfNotExist("menu/include-example/readme.txt", this);
            ResourceUtil.saveIfNotExist("menu/include-example/seller.yml", this);
            ResourceUtil.saveIfNotExist("menu/random-colors/rand-colors-menu.yml", this);
        }
        loader = new MenuLoader(
                new File(getDataFolder(), "menu"),
                this
        );
    }

    @Override
    public void onEnable() {
        BungeeCordMessageSender.registerChannel(this);
        openCommands = new OpenCommands(loader, ResourceUtil.load("config.yml", this));
        loader.loadMenus();
        loader.registerListeners();
        commandWrapper = new CommandWrapper(createCommand(), this);
        commandWrapper.setPermission("bmenu.use");
        commandWrapper.register();
        openCommands.register();
        metrics = new Metrics(this, 23745);
    }

    @Override
    public void onDisable() {
        BungeeCordMessageSender.unregisterChannel(this);
        loader.close();
        commandWrapper.close();
        openCommands.unregister();
        metrics.shutdown();
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
                            openCommands = new OpenCommands(loader, ResourceUtil.load("config.yml", this));
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
                            try {
                                Menu m = loader.findAndCreate(new SpacedNameKey(menu), player, null);
                                m.open();
                            } catch (Throwable t) {
                                player.sendMessage(
                                        Component.text("Меню не удалось открыть с ошибкой: ")
                                                .append(Component.text(t.getMessage()))
                                                .color(TextColor.color(0xFF, 0x55, 0x55))
                                );
                                getSLF4JLogger().error("Failed to open menu {}", menu, t);
                            }
                        })
                )
                .addSubCommand(new Command<CommandSender>("dump")
                        .requires(new RequiresPermission<>("bmenu.dump"))
                        .addSubCommand(new Command<CommandSender>("menu")
                                .requires(new RequiresPermission<>("bmenu.dump.menu"))
                                .argument(new ArgumentSetList<>("menu", () -> loader.getMenus().stream().map(SpacedNameKey::toString).toList()))
                                .executor((sender, args) -> {
                                    String menu = (String) args.getOrThrow("menu", "Use /bmenu dump menu <menu>");
                                    MenuConfig config = loader.findMenu(new SpacedNameKey(menu));
                                    File file = new File(getDataFolder(), "dump-" + System.currentTimeMillis());
                                    file.mkdirs();
                                    config.dump(file.toPath());
                                    loader.getMessage().sendMsg(sender, "&aSaved to {}", file.getPath());
                                })
                        )
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
                )*/;

        return cmd;
    }
}
