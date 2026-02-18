package dev.by1337.bmenu.command.argument;

import dev.by1337.cmd.*;
import dev.by1337.bmenu.animation.util.AnimationUtil;

public class ArgumentSlots<C> extends Argument<C, int[]> {

    public ArgumentSlots(String name) {
        super(name);
    }

    @Override
    public void parse(C c, CommandReader reader, ArgumentMap args) throws CommandMsgError {
        args.put(name, AnimationUtil.readSlots(reader.readString()));
    }

    @Override
    public void suggest(C c, CommandReader reader, SuggestionsList suggestions, ArgumentMap args) throws CommandMsgError {
        suggestions.suggest(reader.readString());
    }

    @Override
    public boolean allowAsync() {
        return true;
    }

    @Override
    public boolean compilable() {
        return true;
    }
}
