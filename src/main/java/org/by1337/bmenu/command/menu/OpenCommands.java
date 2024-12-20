package org.by1337.bmenu.command.menu;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandWrapper;
import org.by1337.blib.command.argument.Argument;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuLoader;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OpenCommands {
    private final MenuLoader loader;
    private final List<OpenCommand> openCommands;

    public OpenCommands(MenuLoader loader, YamlContext config) {
        this.loader = loader;
        openCommands = new ArrayList<>();
        Map<String, YamlContext> map = config.get("open_commands").getAsMap(YamlContext.class, Collections.emptyMap());
        for (String string : map.keySet()) {
            YamlContext ctx = map.get(string);
            String menu = Objects.requireNonNull(ctx.get("menu").getAsString(), "menu is null! In: open_commands." + string);

            List<Argument<CommandSender>> arguments = new ArrayList<>();
            if (ctx.has("tab-completer")){
                ctx.get("tab-completer").mapStream().forEach(pair -> {
                    String name = pair.getLeft().getAsString();
                    YamlContext data = pair.getRight().getAsYamlContext();
                    CommandArgumentType argumentType = CommandArgumentType.valueOf(data.getAsString("type").toUpperCase(Locale.ENGLISH));
                    arguments.add(argumentType.creator().create(data, name));
                });
            }
            OpenCommand openCommand = new OpenCommand(string, menu, arguments, loader.getPlugin());
            openCommand.setAliases(ctx.getList("aliases", String.class, Collections.emptyList()));
            openCommand.setPermission(ctx.getAsString("permission", null));
            openCommands.add(openCommand);
        }
    }

    public void register() {
        for (OpenCommand openCommand : openCommands) {
            openCommand.register();
        }
    }

    public void unregister() {
        for (OpenCommand openCommand : openCommands) {
            openCommand.unregister();
        }
    }

    private class OpenCommand extends Command<CommandSender> {
        private final String menuId;
        private final CommandWrapper wrapper;

        protected OpenCommand(@NotNull String name, String menuId, List<Argument<CommandSender>> arguments, Plugin plugin) {
            super(name);
            this.menuId = menuId;
            arguments.forEach(this::argument);
            wrapper = new CommandWrapper(this, plugin);
            executor(((sender, args) -> {
                if (sender instanceof Player player) {
                    Menu menu = loader.findAndCreate(menuId, player, null);
                    for (String s : args.keySet()) {
                        Object obj = args.get(s);
                        menu.addArgument(s, obj instanceof Player pl ? pl.getName() : String.valueOf(obj));
                    }
                    menu.open();
                }
            }));
        }


        public void setAliases(List<String> aliases) {
            aliases.forEach(this::aliases);
            wrapper.setAliases(aliases);
        }

        public void setPermission(String permission) {
            if (permission != null) {
                requires(new RequiresPermission<>(permission));
            }
            wrapper.setPermission(permission);
        }

        public void register() {
            wrapper.register();
        }

        public void unregister() {
            wrapper.close();
        }
    }
}