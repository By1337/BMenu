/*

package dev.by1337.bmenu.command.argument;

import dev.by1337.bmenu.MenuConfig;
import dev.by1337.bmenu.command.menu.OpenCommands;
import dev.by1337.cmd.*;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuArgumentList extends Argument<CommandSender, Object> {
    private final Map<String, List<Argument<CommandSender, ?>>> arguments;

    public MenuArgumentList(String name, OpenCommands openCommands) {
        super(name);
        arguments = new HashMap<>();
        for (OpenCommands.OpenCommand openCommand : openCommands.openCommands()) {
            arguments.put(openCommand.menuId(), openCommand.arguments());
        }
    }

    @Override
    public void parse(CommandSender sender, CommandReader reader, ArgumentMap args) throws CommandMsgError {
        var list = getArguments(args);
        if (list != null) {
            for (Argument<CommandSender, ?> argument : list) {
                argument.parse(sender, reader, args);
                if (reader.next() == '\0') return;
            }
        } else {
            reader.readString();
        }
    }

    @Override
    public void suggest(CommandSender sender, CommandReader reader, SuggestionsList suggestions, ArgumentMap args) throws CommandMsgError {
        var list = getArguments(args);
        if (list != null) {
            for (Argument<CommandSender, ?> argument : list) {
                argument.suggest(sender, reader, suggestions, args);
                if (reader.next() == '\0') return;
            }
        } else {
            reader.readString();
        }
    }

    @Nullable
    private List<Argument<CommandSender, ?>> getArguments(ArgumentMap map) {
        if (map.get("menu") instanceof MenuConfig cfg) {
            return arguments.get(cfg.getId().asString());
        }
        return null;
    }

   */
/* @Override
    public void process(CommandSender sender, StringReader reader, ArgumentMap<String, Object> argumentMap) throws CommandSyntaxError {
        List<Argument<CommandSender>> args = getArguments(argumentMap);
        if (args != null) {
            for (Argument<CommandSender> arg : args) {
                arg.process(sender, reader, argumentMap);
            }
        }
    }

    @Override
    public void tabCompleter(CommandSender sender, StringReader reader, ArgumentMap<String, Object> argumentMap, SuggestionsBuilder builder) throws CommandSyntaxError {
        List<Argument<CommandSender>> args = getArguments(argumentMap);
        if (args != null) {
            for (Argument<CommandSender> arg : args) {
                arg.tabCompleter(sender, reader, argumentMap, builder);
            }
        }
    }

    @Nullable
    private List<Argument<CommandSender>> getArguments(ArgumentMap<String, Object> map) {
        String menuId = (String) map.get("menu");
        if (menuId != null) {
            return arguments.get(menuId);
        }
        return null;
    }*//*

}

*/
