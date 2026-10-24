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
        return (Player) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Player.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("locale")) return locale.get();
                    if (method.getName().equals("sendMessage") && args.length == 1 && args[0] instanceof Component) {
                        messages.add((Component) args[0]);
                    }
                    return null;
                });
    }

    @Test
    void localizedYamlCommandsAndExplicitGateKeepSlotPlaceholdersDynamic() {
        List<Component> messages = new ArrayList<>();
        var locale = new AtomicReference<>(new Locale("ru", "RU"));
        Player viewer = player(messages, locale);
        var slot = new AtomicReference<>("42");
        Themes.setThemeReplacer(id -> text -> text.replace("{theme}", "theme"));
        var commands = Commands.CODEC.decode(YamlValue.wrap(List.of(
                Map.of("messages-ru_RU", "{theme} RU {slot}"),
                Map.of("message-en_US", "{theme} EN {slot}")))).getOrThrow();
        try (var context = ExecuteContext.of(menu(viewer))) {
            commands.test(context, text -> text.replace("{slot}", slot.get()));
            locale.set(Locale.US);
            slot.set("43");
            commands.test(context, text -> text.replace("{slot}", slot.get()));
            locale.set(new Locale("ru", "BY"));
            commands.test(context, text -> text.replace("{slot}", slot.get()));
            locale.set(Locale.US);
            var explicit = new MenuCommand("[messages_if_lang] en_US explicit {slot}");
            explicit.test(context, text -> text.replace("{slot}", slot.get()));
            assertNotNull(explicit.compiled());
        }
        assertEquals(List.of(Component.text("theme RU 42"), Component.text("theme EN 43"),
                Component.text("explicit 43")), messages);
    }

    private Menu menu(Player viewer) {
        Command<ExecuteContext> commands = MenuTextCommands.create();
        return (Menu) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Menu.class}, (proxy, method, args) -> {
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
        MenuCommand command = new MenuCommand("[message] {theme} {slot}");
        try (var context = ExecuteContext.of(menu)) {
            command.test(context, local);
            assertNotNull(command.compiled());
            assertSame(menu, context.placeholders());
            selected.set("second");
            slot.set("43");
            command.test(context, local);
        }
        assertEquals(List.of(Component.text("first 42"), Component.text("second 43")), messages);
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
        Server server = (Server) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Server.class},
                (proxy, method, args) -> method.getName().equals("getOnlinePlayers") ? List.of(first, second) : null);
        field.set(null, server);
        try (var context = ExecuteContext.of(menu)) {
            new MenuCommand("[BROADCAST] {theme}").test(context, text -> text);
            assertEquals(List.of(Component.text("first")), firstMessages);
            assertEquals(List.of(Component.text("second")), secondMessages);
        } finally {
            field.set(null, previous);
        }
    }
}
