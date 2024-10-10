package org.by1337.bmenu;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.by1337.blib.BLib;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.util.SpacedNameKey;
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
            OpenCommand openCommand = new OpenCommand(string, menu);
            openCommand.setAliases(ctx.getList("aliases", String.class, Collections.emptyList()));
            openCommand.setPermission(ctx.getAsString("permission", null));
            openCommands.add(openCommand);
        }
    }

    public void register() {
        for (OpenCommand openCommand : openCommands) {
            BLib.getApi().getBukkitCommandRegister().register(openCommand);
        }
    }

    public void unregister() {
        for (OpenCommand openCommand : openCommands) {
            BLib.getApi().getBukkitCommandRegister().unregister(openCommand);
        }
    }

    private class OpenCommand extends BukkitCommand {
        private final String menuId;

        protected OpenCommand(@NotNull String name, String menuId) {
            super(name);
            this.menuId = menuId;
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            if (sender instanceof Player player) {
                Menu menu;
                if (this.menuId.contains(":")) {
                    menu = loader.findAndCreate(new SpacedNameKey(menuId), player, null);
                } else {
                    MenuConfig config = loader.findMenuByName(menuId);
                    if (config == null) {
                        menu = null;
                    } else {
                        menu = loader.findAndCreate(config, player, null);
                    }
                }
                if (menu == null) {
                    throw new IllegalArgumentException("Unknown menu: " + menuId);
                }
                menu.open();
            }
            return true;
        }
    }
}
