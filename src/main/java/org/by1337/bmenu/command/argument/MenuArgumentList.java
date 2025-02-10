package org.by1337.bmenu.command.argument;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.command.CommandSender;
import org.by1337.blib.command.CommandSyntaxError;
import org.by1337.blib.command.StringReader;
import org.by1337.blib.command.argument.Argument;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.bmenu.command.menu.OpenCommands;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuArgumentList extends Argument<CommandSender> {
    private final Map<String, List<Argument<CommandSender>>> arguments;

    public MenuArgumentList(String name, OpenCommands openCommands) {
        super(name);
        arguments = new HashMap<>();
        for (OpenCommands.OpenCommand openCommand : openCommands.openCommands()) {
            arguments.put(openCommand.menuId(), openCommand.arguments());
        }
    }

    @Override
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
    }
}
