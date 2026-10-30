package dev.by1337.bmenu.command;

import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.menu.command.MenuTextCommands;
import dev.by1337.cmd.Command;
import dev.by1337.edsl.Themes;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MenuTextCommandsTest {
    @AfterEach
    void reset() {
        Themes.setSelector(null);
        Themes.setThemeReplacer(null);
    }

    private Player player(List<Component> messages) {
        return player(messages, new AtomicReference<>(Locale.US));
    }

    private Player player(List<Component> messages, AtomicReference<Locale> locale) {
        return (Player) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ Player.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("locale")) return locale.get();
                    if (method.getName().equals("sendMessage") && args.length == 1 && args[0] instanceof Component) {
                        messages.add((Component) args[0]);
                    }
                    return null;
                });
    }

    @Test
    void localeMapsKeepSlotPlaceholdersAndThemesDynamic() {
        List<Component> messages = new ArrayList<>();
        var locale = new AtomicReference<>(new Locale("ru", "RU"));
        Player viewer = player(messages, locale);
        var slot = new AtomicReference<>("42");
        Themes.setThemeReplacer(id -> text -> text.replace("{theme}", "theme"));
        var commands = Commands.DECODER.decode(YamlValue.wrap(Map.of("message", Map.of(
                "ru_RU", "{theme} RU {slot}", "en_US", "{theme} EN {slot}")))).getOrThrow();
        try (var context = ExecuteContext.of(menu(viewer))) {
            commands.test(context, text -> text.replace("{slot}", slot.get()));
            locale.set(Locale.US);
            slot.set("43");
            commands.test(context, text -> text.replace("{slot}", slot.get()));
            locale.set(new Locale("ru", "BY"));
            commands.test(context, text -> text.replace("{slot}", slot.get()));
            assertSame(context, context.eventContext().get(ExecuteContext.class));
        }
        assertEquals(List.of(Component.text("theme RU 42"), Component.text("theme EN 43"),
                Component.text("theme RU 43")), messages);
    }

    private Menu menu(Player viewer) {
        Command<ExecuteContext> commands = MenuTextCommands.create();
        return (Menu) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ Menu.class }, (proxy, method, args) -> {
            if (method.getName().equals("viewer")) return viewer;
            if (method.getName().equals("getCommands")) return commands;
            if (method.getName().equals("setPlaceholders")) return args[0];
            if (method.isDefault()) return InvocationHandler.invokeDefault(proxy, method, args == null ? new Object[0] : args);
            return null;
        });
    }

    @Test
    void compiledMessageResolvesThemeBeforeSlotPlaceholdersAndUpdatesOnNextCall() {
        List<Component> messages = new ArrayList<>();
        Player player = player(messages);
        Menu menu = menu(player);
        AtomicReference<String> selected = new AtomicReference<>("first");
        AtomicReference<String> slot = new AtomicReference<>("42");
        Themes.setSelector(p -> selected.get());
        Themes.setThemeReplacer(id -> text -> text.replace("{theme}", id));
        PlaceholderApplier local = text -> {
            assertFalse(text.startsWith("[message]"), "Text must remain raw until argument execution");
            return text.replace("{slot}", slot.get());
        };
        Commands command = new Commands(List.of("[message] {theme} {slot}"));
        try (var context = ExecuteContext.of(menu)) {
            command.test(context, local);
            assertSame(context, context.eventContext().get(ExecuteContext.class));
            assertSame(menu, context.placeholders());
            selected.set("second");
            slot.set("43");
            command.test(context, local);
        }
        assertEquals(List.of(Component.text("first 42"), Component.text("second 43")), messages);
    }

    @Test
    void ordinaryMessagesInsideAllPlayersUseEventTargets() throws Exception {
        List<Component> firstMessages = new ArrayList<>();
        List<Component> secondMessages = new ArrayList<>();
        Player first = player(firstMessages);
        Player second = player(secondMessages);
        var field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        Object previous = field.get(null);
        Server server = (Server) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ Server.class },
                (proxy, method, args) -> method.getName().equals("getOnlinePlayers") ? List.of(first, second) : null);
        try (var context = ExecuteContext.of(menu(first))) {
            context.eventContext();
            field.set(null, server);
            try {
                Commands.DECODER.decode(YamlValue.wrap(Map.of("all_players", Map.of("message", "Hello"))))
                        .getOrThrow().test(context, text -> text);
                assertEquals(List.of(Component.text("Hello")), firstMessages);
                assertEquals(List.of(Component.text("Hello")), secondMessages);
                assertSame(first, context.eventContext().target());
            } finally {
                field.set(null, previous);
            }
        }
    }

    @Test
    void oldAndLocalizedTitlesShareRendererOnThe1165Api() throws Exception {
        var titles = new ArrayList<net.kyori.adventure.title.Title>();
        Player viewer = (Player) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ Player.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("locale")) return Locale.US;
                    if (method.getName().equals("showTitle")) titles.add((net.kyori.adventure.title.Title) args[0]);
                    return null;
                });
        var field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        Object previous = field.get(null);
        try (var context = ExecuteContext.of(menu(viewer))) {
            context.eventContext();
            field.set(null, org.mockito.Mockito.mock(Server.class));
            try {
                Commands.DECODER.decode(YamlValue.wrap(List.of(
                        Map.of("title", Map.of("en_US", "New title\nNew subtitle")),
                        Map.of("title", "\"Legacy\\nSubtitle\" 1 2 3"),
                        Map.of("title", Map.of("en_US", "Separate\nSubtitle\n3\n4\n5")),
                        Map.of("title", Map.of("en_US", "Combined\nSubtitle\n6 7 8")))))
                        .getOrThrow().test(context, text -> text);
            } finally {
                field.set(null, previous);
            }
        }
        assertEquals("1.16.5", dev.by1337.core.ServerVersion.CURRENT_ID);
        assertEquals(4, titles.size());
        assertEquals(Component.text("New title"), titles.get(0).title());
        assertEquals(Component.text("New subtitle"), titles.get(0).subtitle());
        assertEquals(Component.text("Legacy"), titles.get(1).title());
        assertEquals(Component.text("Subtitle"), titles.get(1).subtitle());
        assertEquals(java.time.Duration.ofMillis(50), titles.get(1).times().fadeIn());
        assertEquals(Component.text("Separate"), titles.get(2).title());
        assertEquals(Component.text("Subtitle"), titles.get(2).subtitle());
        assertEquals(java.time.Duration.ofMillis(150), titles.get(2).times().fadeIn());
        assertEquals(java.time.Duration.ofMillis(200), titles.get(2).times().stay());
        assertEquals(java.time.Duration.ofMillis(250), titles.get(2).times().fadeOut());
        assertEquals(Component.text("Combined"), titles.get(3).title());
        assertEquals(Component.text("Subtitle"), titles.get(3).subtitle());
        assertEquals(java.time.Duration.ofMillis(300), titles.get(3).times().fadeIn());
        assertEquals(java.time.Duration.ofMillis(350), titles.get(3).times().stay());
        assertEquals(java.time.Duration.ofMillis(400), titles.get(3).times().fadeOut());
    }

    @Test
    void broadcastUsesEachRecipientsTheme() throws Exception {
        List<Component> firstMessages = new ArrayList<>();
        List<Component> secondMessages = new ArrayList<>();
        Player first = player(firstMessages);
        Player second = player(secondMessages);
        Menu menu = menu(first);
        Themes.setSelector(p -> p == first ? "first" : "second");
        Themes.setThemeReplacer(id -> text -> text.replace("{theme}", id));
        var field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        Object previous = field.get(null);
        Server server = (Server) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ Server.class },
                (proxy, method, args) -> method.getName().equals("getOnlinePlayers") ? List.of(first, second) : null);
        try (var context = ExecuteContext.of(menu)) {
            context.eventContext();
            field.set(null, server);
            try {
                new Commands(List.of("[BROADCAST] {theme}")).test(context, text -> text);
                assertEquals(List.of(Component.text("first")), firstMessages);
                assertEquals(List.of(Component.text("second")), secondMessages);
            } finally {
                field.set(null, previous);
            }
        }
    }
}
