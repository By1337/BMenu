
package dev.by1337.bmenu.command.menu;

import dev.by1337.cmd.*;
import dev.by1337.cmd.argument.ArgumentString;
import dev.by1337.core.command.bcmd.argument.*;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Predicate;

public enum CommandArgumentType {
    PLAYER((ctx, name) -> new Argument<CommandSender, Player>(name) {
        private final ArgumentPlayers<CommandSender> upper = new ArgumentPlayers<>(name, true);

        @Override
        public void parse(CommandSender ctx, CommandReader reader, ArgumentMap args) throws CommandMsgError {
            upper.parse(ctx, reader, args);
            if (args.put(name, null) instanceof List list) {
                if (list.isEmpty()) return;
                args.put(name, list.get(0));
            }
        }

        @Override
        public void suggest(CommandSender ctx, CommandReader reader, SuggestionsList suggestions, ArgumentMap args) throws CommandMsgError {
            upper.suggest(ctx, reader, suggestions, args);
        }
    }),
    INT((ctx, name) -> new ArgumentInt<>(name)),
    BOOL((ctx, name) -> new ArgumentBool<>(name)),
    CHOICE((ctx, name) ->
            new ArgumentChoice<>(
                    name,
                    ctx.get("items").decode(YamlCodec.STRINGS, List.of()).getOrThrow()
            )
    ),
    DOUBLE((ctx, name) -> new ArgumentDouble<>(name)),
    LONG((ctx, name) -> new ArgumentLong<>(name)),
    STRING((ctx, name) -> new ArgumentString<>(name)),

    @Deprecated
    FORMATTED_DOUBLE((ctx, name) -> new ArgumentDouble<>(name)),
    @Deprecated
    LONG_MATH((ctx, name) -> new ArgumentLong<>(name)),
    @Deprecated
    INT_MATH((ctx, name) -> new ArgumentInt<>(name)),
    // WORLD((ctx, name) -> new ArgumentWorld<>(name, getExx(ctx)));
    ;
    public static final YamlCodec<CommandArgumentType> CODEC = YamlCodec.fromEnum(CommandArgumentType.class);
    private final ArgCreator creator;

    CommandArgumentType(ArgCreator creator) {
        this.creator = creator;
    }

    public ArgCreator creator() {
        return creator;
    }

    //   private static List<String> getExx(YamlContext ctx) {
    //       return ctx.getList("exx", String.class, List.of());
    //   }

    private static <T> T check(T t, Predicate<T> predicate, String msg) {
        if (predicate.test(t)) {
            throw new IllegalStateException(msg);
        }
        return t;
    }

    public interface ArgCreator {
        Argument<CommandSender, ?> create(YamlMap context, String name);
    }
}

