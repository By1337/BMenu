package org.by1337.bmenu.factory;


import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.by1337.blib.configuration.YamlConfig;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.NBTParser;
import org.by1337.blib.nbt.impl.ListNBT;
import org.by1337.blib.util.SpacedNameKey;
import org.by1337.bmenu.MenuConfig;
import org.by1337.bmenu.MenuItemBuilder;
import org.by1337.bmenu.MenuLoader;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.command.CommandList;
import org.by1337.bmenu.yaml.RawYamlContext;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

public class MenuFactory {
    private static final MenuFactory INSTANCE = new MenuFactory();

    private MenuFactory() {
    }

    public static MenuConfig load(File file, MenuLoader loader) throws InvalidMenuConfigException {
        try {
            return INSTANCE.load0(file, loader, new MenuLoadContext());
        } catch (InvalidMenuConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidMenuConfigException(e.getMessage(), e);
        }
    }

    private MenuConfig load0(File file, MenuLoader loader, MenuLoadContext loadContext) throws InvalidMenuConfigException, IOException, InvalidConfigurationException {
        loadContext.loadedFiles.add(file);

        RawYamlContext ctx = MenuFilePostprocessor.apply(MenuFilePreprocessor.loadFile(file, loader), loader.getLogger());
        List<MenuConfig> supers = load(FileUtil.findFiles(file, loader, ctx.get("extends").getAsList(YamlValue::getAsString, Collections.emptyList())), loader, loadContext);
        String title = ctx.get("title").getAsString("title is not set!");
        @Nullable SpacedNameKey id = getId(ctx.get("id").getAsString(null), loader);
        @Nullable SpacedNameKey provider = getId(ctx.get("provider").getAsString(null), loader);

        InventoryType type = ctx.get("type").getAs(InventoryType.class, InventoryType.CHEST);
        int size = ctx.get("size").getAsInteger(54);

        List<SpacedNameKey> onlyOpenFrom = ctx.get("only-open-from").getAsList(v -> getId(v.getAsString(), loader), Collections.emptyList());

        Map<String, String> args = ctx.get("args").getAsMap(String.class, Collections.emptyMap());

        Map<String, MenuItemBuilder> items = ItemFactory.readItems(ctx.get("items").getAsMap(YamlValue::getAsYamlContext, Collections.emptyMap()), loader);

        Animator.AnimatorContext animator;
        if (ctx.has("animation")) {
            animator = AnimatorFactory.read(ctx.get("animation").getAsList(YamlValue::getAsYamlContext, Collections.emptyList()), loader);
        } else {
            animator = null;
        }
        CommandList commandList = new CommandList(ctx.get("commands-list"));
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
                animator,
                commandList
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

    private List<MenuConfig> load(List<File> files, MenuLoader loader, MenuLoadContext ctx) throws InvalidMenuConfigException, IOException, InvalidConfigurationException {
        if (files.isEmpty()) return Collections.emptyList();
        List<MenuConfig> result = new ArrayList<>();
        for (File file : files) {
            if (ctx.loadedFiles.contains(file)) continue;
            result.add(load0(file, loader, ctx));
        }
        return result;
    }

    private static class MenuLoadContext {
        private final Set<File> loadedFiles = new HashSet<>();
    }
}
