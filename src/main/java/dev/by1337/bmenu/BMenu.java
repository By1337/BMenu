package dev.by1337.bmenu;


import dev.by1337.bmenu.command.menu.OpenCommands;
import dev.by1337.bmenu.hook.BungeeCordMessageSender;
import dev.by1337.bmenu.hook.Metrics;
import dev.by1337.bmenu.loader.MenuConfig;
import dev.by1337.bmenu.loader.MenuLoader;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.cmd.Command;
import dev.by1337.cmd.argument.ArgumentStrings;
import dev.by1337.core.command.bcmd.CommandError;
import dev.by1337.core.command.bcmd.CommandWrapper;
import dev.by1337.core.command.bcmd.argument.ArgumentDynamicRegistry;
import dev.by1337.core.command.bcmd.argument.ArgumentPlayers;
import dev.by1337.core.command.bcmd.requires.RequiresPermission;
import dev.by1337.core.util.RepositoryUtil;
import dev.by1337.core.util.io.ResourceUtil;
import dev.by1337.core.util.reflect.ClasspathUtil;
import dev.by1337.core.util.text.minimessage.MiniMessage;
import dev.by1337.yaml.YamlMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class BMenu extends JavaPlugin {
    private static final Logger log = LoggerFactory.getLogger("BMenu");
    private static MenuLoader loader;
    private CommandWrapper commandWrapper;
    private OpenCommands openCommands;
    private Metrics metrics;
    private YamlMap config;

    public BMenu() {
        Path libraries = getDataFolder().toPath().resolve(".libraries");
        try {
            ClasspathUtil.addUrl(this, RepositoryUtil.download(RepositoryUtil.BDEV_REPO, "org.by1337.bmenu:BMenu:1.7+legacy", libraries));
        } catch (Exception e) {
            log.warn("Failed to load legacy bmenu", e);
        }
    }

    @Override
    public void onLoad() {
        if (!new File(getDataFolder(), "menu").exists()) {
            ResourceUtil.saveIfNotExist("menu/animation-54.yml", this);
            ResourceUtil.saveIfNotExist("menu/example-seller.yml", this);
            ResourceUtil.saveIfNotExist("menu-shem.yml", this);
            ResourceUtil.saveIfNotExist("menu/include-example/confirm.yml", this);
            ResourceUtil.saveIfNotExist("menu/include-example/seller.yml", this);
            ResourceUtil.saveIfNotExist("menu/random-colors/rand-colors-menu.yml", this);
            ResourceUtil.saveIfNotExist("menu/admin/kick.yml", this);
        }
        config = ResourceUtil.load("config.yml", this);
        loader = new MenuLoader(
                new File(getDataFolder(), "menu"),
                this
                //, config.get("hot-reload").asBool(false) // todo?
        );
        loader.codecRegistry().register("bmenu:default", MenuConfig.CODEC);
        // writeSchemas(this); //todo пока не работает(
    }

    @Override
    public void onEnable() {
        BungeeCordMessageSender.registerChannel(this);
        openCommands = new OpenCommands(loader, config);
        loader.enable();
        commandWrapper = new CommandWrapper(createCommand(), this);
        commandWrapper.setPermission("bmenu.use");
        commandWrapper.register();
        openCommands.register();
        metrics = new Metrics(this, 23745);
    }

    @Override
    public void onDisable() {
        BungeeCordMessageSender.unregisterChannel(this);
        loader.disable();
        commandWrapper.close();
        openCommands.unregister();
        metrics.shutdown();
    }

    public static MenuLoader menuLoader() {
        return loader;
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
                            sender.sendMessage(MiniMessage.deserialize("&aReloaded " + loader.menus().size() + " menus!"));
                        })
                )
                .sub(new Command<CommandSender>("open")
                        .requires(new RequiresPermission<>("bmenu.open"))
                        .argument(new ArgumentDynamicRegistry<>("$menu", () -> loader.menus()))
                        .argument(new ArgumentPlayers<>("$player"))
                        .argument(new ArgumentStrings<>("$custom"))
                        //  .argument(new MenuArgumentList("$custom", openCommands))
                        .executor((sender, args) -> {
                            MenuConfig menu = (MenuConfig) args.getOrThrow("$menu", "Use /bmenu open <menu> <player>");
                            List<Player> players = (List<Player>) args.get("$player");
                            if (players == null) {
                                if (sender instanceof Player) {
                                    players = List.of((Player) sender);
                                } else {
                                    throw new CommandError("Use /bmenu open <menu> <player>");
                                }
                            }
                            for (Player player : players) {
                                try {
                                    String openArgs = (String) args.get("$custom");
                                    if (openArgs != null) {
                                        for (OpenCommands.OpenCommand command : openCommands.openCommands()) {
                                            if (command.menuId().equals(menu.id().asString())) {
                                                command.execute(player, openArgs);
                                                break;
                                            }
                                        }
                                    } else {
                                        Menu m = menu.create(player, null);
                                        m.open();
                                    }
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
                                .argument(new ArgumentDynamicRegistry<>("menu", () -> loader.menus()))
                                .executor((sender, args) -> {
                                    MenuConfig config = (MenuConfig) args.getOrThrow("menu", "Use /bmenu dump menu <menu>");

                                    File file = new File(getDataFolder(), "dump-" + System.currentTimeMillis());
                                    file.mkdirs();
                                    config.dump(file.toPath());
                                    sender.sendMessage(MiniMessage.deserialize("&aSaved to " + file.getPath()));
                                })
                        )
                );

        return cmd;
    }

//    public static void writeSchemas(Plugin plugin) {
//        Plugin bMenu = Bukkit.getPluginManager().getPlugin("BMenu");
//        String bMenuVersion = bMenu == null ? plugin.getDescription().getVersion() : bMenu.getDescription().getVersion();
//        SchemaHolder.addSchema(plugin.getDataFolder(), "bmenu-schema-v" + bMenuVersion + ".json", "menu/**/*.yml", MenuCodec.schema(), "BMenu menu schema");
//    }
//
//    public MenuLoader getMenuLoader() {
//        return loader;
//    }

}
