package dev.by1337.bmenu.command;

import dev.by1337.bmenu.handler.trace.EventTracer;
import dev.by1337.bmenu.event.MenuEventContext;
import dev.by1337.edsl.context.EventContext;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.slot.SlotContent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

public class ExecuteContext implements AutoCloseable, Closeable, PlayerContext {
    private static final Logger log = LoggerFactory.getLogger("BMenu");
    public static boolean ENABLE_TRACER = false;
    public Menu menu;
    public @Nullable SlotContent item;
    public final EventTracer tracer;
    private @Nullable StringBuilder tracerLog;
    private @Nullable PlaceholderApplier placeholders;
    private @Nullable EventContext eventContext;

    private ExecuteContext(Menu menu, String cause) {
        this(menu, null, cause);
    }

    private ExecuteContext(Menu menu, @Nullable SlotContent item, @Nullable String cause) {
        this.menu = menu;
        this.item = item;
        if (ENABLE_TRACER) {
            tracerLog = new StringBuilder();
            if (cause != null) {
                tracerLog.append(cause).append("\n");
            }
            tracer = EventTracer.performanceTracer(tracerLog);
        } else {
            tracer = EventTracer.NOP;
        }
    }

    public EventContext eventContext() {
        if (eventContext == null) {
            eventContext = new MenuEventContext(this,
                    text -> placeholders().setPlaceholders(text));
        }
        return eventContext;
    }

    public PlaceholderApplier placeholders() {
        return placeholders == null ? menu : placeholders;
    }

    public Scope withPlaceholders(PlaceholderApplier current) {
        PlaceholderApplier previous = placeholders;
        placeholders = current;
        return () -> placeholders = previous;
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }

    public void flushTracer() {
        if (tracerLog == null) return;
        log.info("TRACER:\n{}", tracerLog);
    }

    public static ExecuteContext of(Menu menu) {
        return of(menu, (String) null);
    }

    public static ExecuteContext of(Menu menu, String cause) {
        return new ExecuteContext(menu, cause);
    }

    public static ExecuteContext of(Menu menu, @Nullable SlotContent item) {
        return of(menu, item, null);
    }

    public static ExecuteContext of(Menu menu, @Nullable SlotContent item, String cause) {
        return new ExecuteContext(menu, item, cause);
    }

    @Override
    public void close() {
        flushTracer();
    }

    @Override
    public Player getPlayer() {
        return menu.viewer();
    }

    @Override
    public EventTracer tracer() {
        return tracer;
    }
}
