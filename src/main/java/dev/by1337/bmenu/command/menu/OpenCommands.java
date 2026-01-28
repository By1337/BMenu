package dev.by1337.bmenu.command.menu;

import dev.by1337.cmd.Argument;
import dev.by1337.cmd.Command;
import dev.by1337.core.command.bcmd.CommandWrapper;
import dev.by1337.core.command.bcmd.requires.RequiresPermission;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.MenuLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OpenCommands {
    private final MenuLoader loader;
    private final List<OpenCommand> openCommands;

    public OpenCommands(MenuLoader loader, YamlMap config) {
        this.loader = loader;
        openCommands = new ArrayList<>();
        Map<String, OpenCommandConfig> commands = config.get("open_commands")
                .decode(YamlCodec.mapOf(YamlCodec.STRING, OpenCommandConfig.CODEC), Map.of()).getOrThrow();
        for (String cmd : commands.keySet()) {
            OpenCommandConfig cfg = commands.get(cmd);
            List<Argument<CommandSender, ?>> arguments = new ArrayList<>();
           if (cfg.suggestions != null){
               for (String name : cfg.suggestions.keySet()) {
                   YamlMap data = cfg.suggestions.get(name);
                   CommandArgumentType argumentType = data.get("type").decode(CommandArgumentType.CODEC).getOrThrow();
                   arguments.add(argumentType.creator().create(data, name));
               }
           }
            OpenCommand openCommand = new OpenCommand(cmd, cfg.menu, arguments, loader.getPlugin());
            openCommand.setAliases(cfg.aliases);
            openCommand.setPermission(cfg.permission);
            openCommands.add(openCommand);
        }
    }

    public List<OpenCommand> openCommands() {
        return openCommands;
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

    public record OpenCommandConfig(
            String menu,
            List<String> aliases,
            @Nullable String permission,
            @Nullable Map<String, YamlMap> suggestions
    ) {
        public static final YamlCodec<OpenCommandConfig> CODEC = RecordYamlCodecBuilder.mapOf(
                OpenCommandConfig::new,
                YamlCodec.STRING.fieldOf("menu", OpenCommandConfig::menu),
                YamlCodec.STRINGS.fieldOf("aliases", OpenCommandConfig::aliases, List.of()),
                YamlCodec.STRING.fieldOf("permission", OpenCommandConfig::permission),
                YamlCodec.STRING_TO_YAML_MAP
                        .fieldOf("tab-completer", OpenCommandConfig::suggestions)
                );

        public OpenCommandConfig {
            Objects.requireNonNull(menu, "menu is null");
        }
    }
    public class OpenCommand extends Command<CommandSender> {
        private final String menuId;
        private final CommandWrapper wrapper;
        private final List<Argument<CommandSender, ?>> arguments;

        protected OpenCommand(@NotNull String name, String menuId, List<Argument<CommandSender, ?>> arguments, Plugin plugin) {
            super(name);
            this.menuId = menuId;
            this.arguments = arguments;
            arguments.forEach(this::argument);
            wrapper = new CommandWrapper(this, plugin);
            executor(((sender, args) -> {
                if (sender instanceof Player player) {
                    Menu menu = loader.create(menuId, player, null);
                    for (String s : args.keys()) {
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

        public String menuId() {
            return menuId;
        }

        public List<Argument<CommandSender, ?>> arguments() {
            return arguments;
        }
    }
}