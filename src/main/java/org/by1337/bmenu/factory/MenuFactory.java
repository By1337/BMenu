package org.by1337.bmenu.factory;


import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.inventory.InventoryType;
import org.by1337.blib.configuration.YamlConfig;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.blib.util.SpacedNameKey;
import org.by1337.bmenu.MenuConfig;
import org.by1337.bmenu.MenuItemBuilder;
import org.by1337.bmenu.MenuLoader;
import org.by1337.bmenu.animation.Animator;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MenuFactory {
    private static final MenuFactory INSTANCE = new MenuFactory();

    private MenuFactory() {
    }

    public static MenuConfig load(File file, MenuLoader loader) throws InvalidMenuConfigException {
        try {
            return INSTANCE.load0(file, loader);
        } catch (InvalidMenuConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidMenuConfigException(e.getMessage(), e);
        }
    }

    public MenuConfig load0(File file, MenuLoader loader) throws InvalidMenuConfigException, IOException, InvalidConfigurationException {
        YamlContext ctx = new YamlConfig(file);
        List<MenuConfig> supers = load(findFiles(file, loader, ctx.getList("extends", String.class, Collections.emptyList())), loader);
        String title = ctx.getAsString("title", "title is not set!");
        @Nullable SpacedNameKey id = getId(ctx.getAsString("id", null), loader);
        @Nullable SpacedNameKey provider = getId(ctx.getAsString("provider", null), loader);

        InventoryType type = ctx.getAs("type", InventoryType.class, InventoryType.CHEST);
        int size = ctx.getAsInteger("size", 54);

        List<SpacedNameKey> onlyOpenFrom = ctx.get("only-open-from").getAsList(v -> getId(v.getAsString(), loader), Collections.emptyList());

        Map<String, String> args = ctx.getMap("args", String.class, Collections.emptyMap());

        Map<String, MenuItemBuilder> items = ItemFactory.readItems(ctx.get("items").getAsMap(YamlValue::getAsYamlContext, Collections.emptyMap()), loader);

        Animator.AnimatorContext animator;
        if (ctx.getHandle().contains("animation")) {
            animator = AnimatorFactory.read(ctx.get("animation").getAsList(YamlValue::getAsYamlContext, Collections.emptyList()), loader);
        } else {
            animator = null;
        }

        return new MenuConfig(
                supers,
                id,
                provider,
                type,
                size,
                onlyOpenFrom,
                args,
                items,
                ctx,
                loader,
                title,
                animator
        );
    }

    private SpacedNameKey getId(@Nullable String id, MenuLoader loader) {
        if (id == null) return null;
        if (id.contains(":")) {
            return new SpacedNameKey(id);
        } else {
            return new SpacedNameKey(loader.getPlugin().getName(), id);
        }
    }

    private List<File> findFiles(File file, MenuLoader loader, List<String> files) throws InvalidMenuConfigException {
        if (files.isEmpty()) return Collections.emptyList();
        File fileFolder = file.getParentFile();
        List<File> result = new ArrayList<>();
        for (String s : files) {
            if (s.startsWith("./")) {
                File menu = new File(fileFolder, s.substring(2));
                if (!menu.exists()) {
                    throw new InvalidMenuConfigException("Invalid menu path: {}", menu.getAbsolutePath());
                }
                result.add(menu);
            } else if (s.startsWith("../")) {
                File menu = new File(fileFolder.getParent(), s.substring(3));
                if (!menu.exists()) {
                    throw new InvalidMenuConfigException("Invalid menu path: {}", menu.getAbsolutePath());
                }
                result.add(menu);
            } else if (s.startsWith("/")) {
                File menu = new File(loader.getHomeDir(), s.substring(1));
                if (!menu.exists()) {
                    throw new InvalidMenuConfigException("Invalid menu path: {}", menu.getAbsolutePath());
                }
                result.add(menu);
            }
        }
        return result;
    }

    private List<MenuConfig> load(List<File> files, MenuLoader loader) throws InvalidMenuConfigException {
        if (files.isEmpty()) return Collections.emptyList();
        List<MenuConfig> result = new ArrayList<>();
        for (File file : files) {
            result.add(load(file, loader));
        }
        return result;
    }
}
