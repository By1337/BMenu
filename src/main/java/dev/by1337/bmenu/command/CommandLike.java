package dev.by1337.bmenu.command;

import dev.by1337.plc.PlaceholderApplier;

public interface CommandLike {
    void run(ExecuteContext ctx, PlaceholderApplier placeholders);
}
