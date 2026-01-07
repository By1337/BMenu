package dev.by1337.bmenu;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import dev.by1337.bmenu.impl.DefaultMenu;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.registry.RegistryLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MenuRegistry extends RegistryLike<MenuRegistry.MenuCreator> {
    public static final MenuRegistry DEFAULT_REGISTRY = new MenuRegistry();


    public void register(final String key, final MenuSupplier value) {
        register(Objects.requireNonNull(NamespacedKey.fromString(key)), value);
    }

    public void merge(RegistryLike<MenuRegistry.MenuCreator> other) {
        other.stream().forEach(this::register);
    }

    public void register(final NamespacedKey key, final MenuSupplier value) {
        register(new MenuCreator() {
            @Override
            public @NotNull NamespacedKey getKey() {
                return key;
            }

            @Override
            public Menu createMenu(MenuConfig config, Player viewer, @Nullable Menu previousMenu) {
                return value.createMenu(config, viewer, previousMenu);
            }
        });
    }

    public interface MenuCreator extends Keyed, MenuSupplier {
    }

    public interface MenuSupplier {
        Menu createMenu(MenuConfig config, Player viewer, @Nullable Menu previousMenu);
    }

    static {
        DEFAULT_REGISTRY.register("bmenu:default", DefaultMenu::new);
    }
}
