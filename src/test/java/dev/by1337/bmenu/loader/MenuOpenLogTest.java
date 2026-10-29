package dev.by1337.bmenu.loader;

import dev.by1337.bmenu.menu.Menu;
import dev.by1337.vault.BVaultApi;
import dev.by1337.vault.ServerLogger;
import dev.by1337.vault.log.LogDataWriter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.helpers.NOPLogger;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuOpenLogTest {
    @TempDir
    Path directory;

    private MenuLoader loader() {
        var plugin = mock(Plugin.class);
        when(plugin.getSLF4JLogger()).thenReturn(NOPLogger.NOP_LOGGER);
        return new MenuLoader(directory.toFile(), plugin);
    }

    private AutoCloseable logger(ServerLogger logger) throws ReflectiveOperationException {
        var field = BVaultApi.class.getDeclaredField("logger");
        field.setAccessible(true);
        Object previous = field.get(null);
        field.set(null, logger);
        return () -> field.set(null, previous);
    }

    private LogDataWriter writer(Map<String, Object> values) {
        return (LogDataWriter) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ LogDataWriter.class },
                (proxy, method, args) -> {
                    if (method.getName().startsWith("set")) {
                        values.put((String) args[0], args[1]);
                        return proxy;
                    }
                    return switch (method.getName()) {
                        case "toString" -> values.toString();
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        default -> throw new UnsupportedOperationException(method.getName());
                    };
                });
    }

    @Test
    void openingCapturesPlayerAndMenuBeforeTheAsynchronousLogIsWritten() throws Exception {
        UUID who = UUID.randomUUID();
        var player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(who);
        var menu = mock(Menu.class);
        when(menu.getId()).thenReturn(new NamespacedKey("buyer", "sell"));
        var pending = new AtomicReference<Consumer<LogDataWriter>>();
        var loader = loader();
        try (var ignored = logger(pending::set)) {
            loader.onMenuOpen(menu, player);
            assertSame(menu, loader.getOpenedMenu(player));
            assertNotNull(pending.get());
            clearInvocations(menu, player);

            Map<String, Object> values = new HashMap<>();
            pending.get().accept(writer(values));
            assertEquals(Map.of("type", "bmenu:open", "who", who, "menu", "buyer:sell"), values);
            verifyNoInteractions(menu, player);
        }
    }

    @Test
    void anonymousMenuGetsAStableIdInsteadOfANullLogValue() throws Exception {
        var player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        var menu = mock(Menu.class);
        Map<String, Object> values = new HashMap<>();
        try (var ignored = logger(log -> log.accept(writer(values)))) {
            loader().onMenuOpen(menu, player);
            assertEquals("minecraft:unnamed", values.get("menu"));
        }
    }

    @Test
    void reopeningTheSameMenuRecordsAnotherOpening() throws Exception {
        var player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        var menu = mock(Menu.class);
        when(menu.getId()).thenReturn(new NamespacedKey("example", "shop"));
        var count = new AtomicInteger();
        var loader = loader();
        try (var ignored = logger(log -> count.incrementAndGet())) {
            loader.onMenuOpen(menu, player);
            loader.onMenuOpen(menu, player);
            assertEquals(2, count.get());
            assertSame(menu, loader.getOpenedMenu(player));
        }
    }
}
