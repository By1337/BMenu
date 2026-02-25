package dev.by1337.bmenu.command;

import dev.by1337.bmenu.handler.trace.EventTracer;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.slot.SlotContent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

public class ExecuteContext implements AutoCloseable, Closeable {
    private static final Logger log = LoggerFactory.getLogger("BMenu");
    public static boolean ENABLE_TRACER = false;
    public Menu menu;
    public @Nullable SlotContent item;
    public final EventTracer tracer;
    private @Nullable StringBuilder tracerLog;

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
}
