/*
package org.by1337.bmenu.command.menu;

import dev.by1337.cmd.Argument;
import dev.by1337.yaml.YamlMap;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.Predicate;

public enum CommandArgumentType {
//   PLAYER((ctx, name) -> new ArgumentPlayer<>(name, getExx(ctx))),
//   INT((ctx, name) -> new ArgumentInteger<>(
//           name,
//           getExx(ctx),
//           ctx.getAsInteger("min", Integer.MIN_VALUE),
//           ctx.getAsInteger("max", Integer.MAX_VALUE)
//   )),
//   BOOL((ctx, name) -> new ArgumentBoolean<>(name)),
//   CHOICE((ctx, name) ->
//           new ArgumentChoice<>(
//                   name,
//                   check(
//                           ctx.getList("items", String.class, List.of()),
//                           List::isEmpty,
//                           "The argument of type CHOICE must necessarily contain items"
//                   )
//           )
//   ),
//   DOUBLE((ctx, name) -> new ArgumentDouble<>(
//           name,
//           getExx(ctx),
//           ctx.getAsDouble("min", Double.MIN_VALUE),
//           ctx.getAsDouble("max", Double.MAX_VALUE)
//   )),
//   FORMATTED_DOUBLE((ctx, name) -> new ArgumentFormattedDouble<>(name, getExx(ctx))),
//   LONG((ctx, name) -> new ArgumentLong<>(
//           name,
//           getExx(ctx),
//           ctx.getAsLong("min", Long.MIN_VALUE),
//           ctx.getAsLong("max", Long.MAX_VALUE)
//   )),
//   LONG_MATH((ctx, name) -> new ArgumentLongAllowedMath<>(
//           name,
//           getExx(ctx)
//   )),
//   INT_MATH((ctx, name) -> new ArgumentIntegerAllowedMath<>(
//           name,
//           getExx(ctx),
//           ctx.getAsInteger("min", Integer.MIN_VALUE),
//           ctx.getAsInteger("max", Integer.MAX_VALUE)
//   )),
//   STRING((ctx, name) -> new ArgumentString<>(name, getExx(ctx))),
//   WORLD((ctx, name) -> new ArgumentWorld<>(name, getExx(ctx)));
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
*/
