package dev.by1337.bmenu;


import dev.by1337.cmd.Command;
import dev.by1337.core.command.bcmd.CommandError;
import dev.by1337.core.command.bcmd.CommandWrapper;
import dev.by1337.core.command.bcmd.argument.ArgumentDynamicRegistry;
import dev.by1337.core.command.bcmd.argument.ArgumentPlayers;
import dev.by1337.core.command.bcmd.requires.RequiresPermission;
import dev.by1337.core.util.io.ResourceUtil;
import dev.by1337.core.util.text.minimessage.MiniMessage;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.schema.SchemaHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import dev.by1337.bmenu.command.menu.OpenCommands;
import dev.by1337.bmenu.factory.MenuCodec;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.metrics.Metrics;
import dev.by1337.bmenu.network.BungeeCordMessageSender;

import java.io.File;
import java.util.List;

public class BMenu extends JavaPlugin {
    private MenuLoader loader;
    private CommandWrapper commandWrapper;
    private OpenCommands openCommands;
    private Metrics metrics;
    private YamlMap config;

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
            ResourceUtil.saveIfNotExist("menu/admin/kick.yml", this);
        }
        config = ResourceUtil.load("config.yml", this);
        loader = new MenuLoader(
                new File(getDataFolder(), "menu"),
                this,
                config.get("hot-reload").asBool(false)
        );
        writeSchemas(this);
    }

    @Override
    public void onEnable() {
        BungeeCordMessageSender.registerChannel(this);
        openCommands = new OpenCommands(loader, config);
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
                .sub(new Command<CommandSender>("reload")
                        .requires(new RequiresPermission<>("bmenu.reload"))
                        .executor((sender, args) -> {
                            loader.reload();
                            openCommands.unregister();
                            openCommands = new OpenCommands(loader, ResourceUtil.load("config.yml", this));
                            openCommands.register();
                            sender.sendMessage(MiniMessage.deserialize("&aReloaded " + loader.getMenuRegistry().size() + " menus!"));
                        })
                )
                .sub(new Command<CommandSender>("open")
                        .requires(new RequiresPermission<>("bmenu.open"))
                        .argument(new ArgumentDynamicRegistry<>("menu", () -> loader.getMenuRegistry()))
                        .argument(new ArgumentPlayers<>("player"))
                        //  .argument(new MenuArgumentList("custom", openCommands))
                        .executor((sender, args) -> {
                            MenuConfig menu = (MenuConfig) args.getOrThrow("menu", "Use /bmenu open <menu> <player>");
                            List<Player> players = (List<Player>) args.get("player");
                            if (players == null) {
                                if (sender instanceof Player) {
                                    players = List.of((Player) sender);
                                } else {
                                    throw new CommandError("Use /bmenu open <menu> <player>");
                                }
                            }
                            for (Player player : players) {
                                try {
                                    Menu m = loader.create(menu, player, null);

                                    // for (String s : args.keySet()) {
                                    //     if (s.equals("menu") || s.equals("player") || s.equals("custom")) continue;
                                    //     Object obj = args.get(s);
                                    //     m.addArgument(s, obj instanceof Player pl ? pl.getName() : String.valueOf(obj));
                                    // }

                                    m.open();
                                } catch (Exception t) {
                                    sender.sendMessage(
                                            Component.text("Меню не удалось открыть с ошибкой: ")
                                                    .append(Component.text(t.getMessage()))
                                                    .color(TextColor.color(0xFF, 0x55, 0x55))
                                    );
                                    getSLF4JLogger().error("Failed to open menu {}", menu, t);
                                    break;
                                }
                            }

                        })
                )
                .sub(new Command<CommandSender>("dump")
                        .requires(new RequiresPermission<>("bmenu.dump"))
                        .sub(new Command<CommandSender>("menu")
                                .requires(new RequiresPermission<>("bmenu.dump.menu"))
                                .argument(new ArgumentDynamicRegistry<>("menu", () -> loader.getMenuRegistry()))
                                .executor((sender, args) -> {
                                    MenuConfig config = (MenuConfig) args.getOrThrow("menu", "Use /bmenu dump menu <menu>");

                                    File file = new File(getDataFolder(), "dump-" + System.currentTimeMillis());
                                    file.mkdirs();
                                    config.dump(file.toPath());
                                    sender.sendMessage(MiniMessage.deserialize("&aSaved to " + file.getPath()));
                                })
                        )
                )
                .sub(new Command<CommandSender>("test")
                        .sub(new Command<CommandSender>("item")
                                .executor((sender, args) -> {
                                    // LegacyItemLike legacyItemLike = new LegacyItemLike();
                                    // legacyItemLike.setMaterial(Material.DIAMOND_AXE);
                                    // legacyItemLike.setCount(64);
                                    // legacyItemLike.setName(LegacyConvertor.convert0("<red>CustomName"));
                                    // legacyItemLike.setLore(List.of(
                                    //         LegacyConvertor.convert0("<red>Lore 1"),
                                    //         LegacyConvertor.convert0("<red>Lore 2"),
                                    //         LegacyConvertor.convert0("<red>Lore 3")
                                    // ));
                                    // getSLF4JLogger().info("Test Item {}", legacyItemLike.writeToString());
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

    public static void writeSchemas(Plugin plugin) {
        Plugin bMenu = Bukkit.getPluginManager().getPlugin("BMenu");
        String bMenuVersion = bMenu == null ? plugin.getDescription().getVersion() : bMenu.getDescription().getVersion();
        SchemaHolder.addSchema(plugin.getDataFolder(), "bmenu-schema-v" + bMenuVersion + ".json", "menu/**/*.yml", MenuCodec.schema(), "BMenu menu schema");
    }

    public MenuLoader getMenuLoader() {
        return loader;
    }
}
