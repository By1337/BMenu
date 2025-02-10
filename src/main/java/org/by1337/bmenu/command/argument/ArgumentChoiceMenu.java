package org.by1337.bmenu.command.argument;


import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.by1337.blib.command.CommandSyntaxError;
import org.by1337.blib.command.StringReader;
import org.by1337.blib.command.argument.Argument;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.argument.ArgumentUtils;
import org.by1337.blib.lang.Lang;

import java.util.List;
import java.util.function.Supplier;

public class ArgumentChoiceMenu<T> extends Argument<T> {
    public final Supplier<List<String>> items;

    public ArgumentChoiceMenu(String name, Supplier<List<String>> items) {
        super(name, items);
        this.items = items;
    }


    @Override
    public void process(T sender, StringReader reader, ArgumentMap<String, Object> argumentMap) throws CommandSyntaxError {
        String str = reader.hasNext() ? ArgumentUtils.readString(reader) : "";
        if (str.isEmpty()) {
            return;
        }
        List<String> list = items.get();
        if (!list.contains(str)) {
            if (list.size() > 10)
                throw new CommandSyntaxError(Lang.getMessage("constant-not-found-more"), str, list.subList(0, 5), list.size() - 10);
            throw new CommandSyntaxError(Lang.getMessage("constant-not-found"), str, items.get());
        }
        argumentMap.put(name, str);
    }

    @Override
    public void tabCompleter(T sender, StringReader reader, ArgumentMap<String, Object> argumentMap, SuggestionsBuilder builder) throws CommandSyntaxError {
        String str = reader.hasNext() ? ArgumentUtils.readString(reader) : "";
        if (str.isEmpty())
            addSuggestions(builder, ArgumentUtils.quoteAndEscapeIfNeeded(items.get()));
        else {
            var list = items.get().stream().filter(s -> s.startsWith(str)).toList();
            if (list.size() == 1){
                argumentMap.put(name, list.get(0));
            }
            addSuggestions(builder, ArgumentUtils.quoteAndEscapeIfNeeded(list));
        }

    }
}