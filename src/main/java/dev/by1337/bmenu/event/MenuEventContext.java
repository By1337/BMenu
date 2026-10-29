package dev.by1337.bmenu.event;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.command.PlayerContext;
import dev.by1337.edsl.context.EventContext;
import dev.by1337.cmd.Command;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import dev.by1337.edsl.trace.EventTracer;
import dev.by1337.plc.PlaceholderApplier;
import org.bukkit.entity.Player;

/** Bridges the public BMenu execution context to EventDSL. */
public final class MenuEventContext extends EventContext {
    private static final Map<Command<ExecuteContext>, WeakReference<Command<EventContext>>> COMMANDS = new WeakHashMap<>();
    private final PlayerContext player;
    private final PlaceholderApplier placeholders;
    private final EventTracer trace;

    public MenuEventContext(PlayerContext player, PlaceholderApplier placeholders) {
        super(false, player == null ? null : player.getPlayer());
        this.player = player;
        // Root dependency lives as long as this context, including delayed commands.
        if (player != null) with(player);
        this.placeholders = placeholders;
        var original = player == null ? dev.by1337.bmenu.handler.trace.EventTracer.NOP : player.tracer();
        trace = new EventTracer() {
            @Override
            public TracerDepth enter(String name, String exit) {
                var scope = original.enter(name, exit);
                return new TracerDepth() {
                    @Override
                    public void close() {
                        scope.close();
                    }

                    @Override
                    public void result(boolean result) {
                        scope.result(result);
                    }
                };
            }

            @Override
            public void log(String value, Object... args) {
                original.log(value, args);
            }

            @Override
            public void log(String value) {
                original.log(value);
            }
        };
    }

    @Override
    public EventTracer tracer() {
        return trace;
    }

    @Override
    public Command<EventContext> command() {
        if (player instanceof ExecuteContext execution) {
            var original = execution.menu.getCommands();
            synchronized (COMMANDS) {
                var reference = COMMANDS.get(original);
                var mapped = reference == null ? null : reference.get();
                if (mapped == null) {
                    mapped = original.map(ctx -> ctx.get(ExecuteContext.class));
                    COMMANDS.put(original, new WeakReference<>(mapped));
                }
                return mapped;
            }
        }
        return super.command();
    }

    @Override
    public Player source() {
        return player == null ? null : player.getPlayer();
    }

    @Override
    public Player target() {
        Player target = super.target();
        return target == null ? source() : target;
    }

    @Override
    public String setPlaceholders(String source) {
        return placeholders == null ? source : placeholders.setPlaceholders(source);
    }
}
