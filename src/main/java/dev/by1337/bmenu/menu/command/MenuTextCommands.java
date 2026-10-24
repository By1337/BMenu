package dev.by1337.bmenu.menu.command;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.cmd.Command;
import dev.by1337.core.util.text.AdventureAdapter;
import dev.by1337.cmd.CommandMsgError;
import dev.by1337.cmd.argument.ArgumentString;
import dev.by1337.edsl.command.args.ArgumentRawText;
import dev.by1337.edsl.command.args.ArgumentRawTitle;
import dev.by1337.edsl.command.LocalizedTextCommands;
import dev.by1337.edsl.text.RawTextComponentLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;

/** BMenu keeps its event language, while EventDSL owns theme-aware text. */
public final class MenuTextCommands {
    private static final Set<String> COMMANDS = Set.of("[message]", "[messages]", "[broadcast]", "[bc]", "[message_all]",
            "[action_bar]", "[action_bar_all]", "[title]", "[title_all]");

    private MenuTextCommands() {}

    public static boolean defersPlaceholders(String source) {
        int end = source.indexOf(' ');
        return COMMANDS.contains((end < 0 ? source : source.substring(0, end)).toLowerCase(Locale.ROOT))
                || LocalizedTextCommands.defersPlaceholders(source);
    }

    public static Command<ExecuteContext> create() {
        return new Command<ExecuteContext>("root")
                .and(LocalizedTextCommands.<ExecuteContext>create(
                        ExecuteContext::getPlayer,
                        ExecuteContext::placeholders,
                        (ctx, output) -> {
                            Bukkit.getOnlinePlayers().forEach(output);
                        }))
                .sub(new Command<ExecuteContext>("[MESSAGE]").aliases("[message]", "[messages]").executor(
                        new ArgumentRawText<>("text"),
                        (ctx, text) -> {
                            ctx.getPlayer().sendMessage(render(text, ctx.getPlayer(), ctx));
                        }))
                .sub(new Command<ExecuteContext>("[BROADCAST]").aliases("[broadcast]", "[bc]", "[message_all]").executor(
                        new ArgumentRawText<>("text"),
                        (ctx, text) -> {
                            for (Player player : Bukkit.getOnlinePlayers()) player.sendMessage(render(text, player, ctx));
                        }))
                .sub(new Command<ExecuteContext>("[ACTION_BAR]").alias("[action_bar]").executor(
                        new ArgumentRawText<>("text"),
                        (ctx, text) -> {
                            ctx.getPlayer().sendActionBar(render(text, ctx.getPlayer(), ctx));
                        }))
                .sub(new Command<ExecuteContext>("[ACTION_BAR_ALL]").alias("[action_bar_all]").executor(
                        new ArgumentRawText<>("text"),
                        (ctx, text) -> {
                            for (Player player : Bukkit.getOnlinePlayers()) player.sendActionBar(render(text, player, ctx));
                        }))
                .sub(title(false))
                .sub(title(true));
    }

    private static Command<ExecuteContext> title(boolean all) {
        return new Command<ExecuteContext>(all ? "[TITLE_ALL]" : "[TITLE]")
                .alias(all ? "[title_all]" : "[title]")
                .executor(
                        new ArgumentRawTitle<>("text"),
                        new ArgumentString<>("fadeIn"),
                        new ArgumentString<>("stay"),
                        new ArgumentString<>("fadeOut"),
                        (ctx, text, fadeIn, stay, fadeOut) -> {
                            if (text == null) throw new CommandMsgError("Use: [title] <\"title\\nsubtitle\"> [fadeIn] [stay] [fadeOut]");
                            Title.Times times = AdventureAdapter.titleTimes(
                                    Ticks.duration(duration(fadeIn, 10, ctx)),
                                    Ticks.duration(duration(stay, 70, ctx)),
                                    Ticks.duration(duration(fadeOut, 20, ctx)));
                            if (all) {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    player.showTitle(text.render(player, ctx.placeholders(), times));
                                }
                            } else {
                                ctx.getPlayer().showTitle(text.render(ctx.getPlayer(), ctx.placeholders(), times));
                            }
                        });
    }

    private static Component render(@Nullable RawTextComponentLike text, Player player, ExecuteContext context) {
        return text == null ? Component.empty() : text.asComponent(player, context.placeholders());
    }

    private static int duration(@Nullable String value, int fallback, ExecuteContext context) {
        if (value == null) return fallback;
        String resolved = context.placeholders().setPlaceholders(value);
        try {
            return Integer.parseInt(resolved);
        } catch (NumberFormatException e) {
            throw new CommandMsgError("Expected integer duration: " + resolved);
        }
    }
}
