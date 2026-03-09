package dev.by1337.bmenu.command.argument;

import dev.by1337.bmenu.loader.MenuConfig;
import dev.by1337.bmenu.loader.MenuLoader;
import dev.by1337.cmd.ArgumentMap;
import dev.by1337.cmd.CommandMsgError;
import dev.by1337.cmd.CommandReader;
import dev.by1337.cmd.SuggestionsList;
import dev.by1337.core.command.bcmd.argument.ArgumentRegistry;

public class ArgumentMenuConfig<C> extends ArgumentRegistry<C, MenuConfig> {

    private final MenuLoader loader;
    private long timestamp;

    public ArgumentMenuConfig(String name, MenuLoader loader) {
        super(name, loader.menus());
        this.loader = loader;
        timestamp = loader.menus().lastMutatedTimestamp();
    }

    protected void rebuildIfNeeded() {
        if (loader.menus().lastMutatedTimestamp() != timestamp) {
            lookup.clear();
            build(loader.menus(), false);
            timestamp = loader.menus().lastMutatedTimestamp();
        }
    }

    @Override
    public void parse(C ctx, CommandReader reader, ArgumentMap out) throws CommandMsgError {
        rebuildIfNeeded();
        int idx = reader.ridx();
        String str = reader.readString();
        MenuConfig cfg = loader.findMenuConfig(str);
        if (cfg != null) {
            out.put(name, cfg);
        } else {
            reader.ridx(idx);
            super.parse(ctx, reader, out);
        }
    }

    @Override
    public void suggest(C ctx, CommandReader reader, SuggestionsList suggestions, ArgumentMap args) throws CommandMsgError {
        rebuildIfNeeded();
        super.suggest(ctx, reader, suggestions, args);
    }

    @Override
    public boolean compilable() {
        return false;
    }
}
