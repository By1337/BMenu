package org.by1337.bmenu.factory;


import dev.by1337.yaml.BukkitYamlCodecs;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.inventory.InventoryType;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.blib.util.SpacedNameKey;
import org.by1337.bmenu.MenuConfig;
import org.by1337.bmenu.MenuItemBuilder;
import org.by1337.bmenu.MenuLoader;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.command.CommandList;
import org.by1337.bmenu.requirement.CommandRequirements;
import org.by1337.bmenu.requirement.Requirements;
import org.by1337.bmenu.util.ObjectUtil;
import org.by1337.bmenu.yaml.RawYamlContext;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class MenuFactory {

    private static final MenuFactory INSTANCE = new MenuFactory();

    private MenuFactory() {
    }

    public static MenuConfig load(File file, MenuLoader loader) throws InvalidMenuConfigException {
        try {
            MenuCodec codec = new MenuCodec(file, loader);
            return codec.decode();
        } catch (InvalidMenuConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidMenuConfigException(e.getMessage(), e);
        }
    }

    /*private MenuConfig load0(File file, MenuLoader loader, MenuLoadContext loadContext) throws InvalidMenuConfigException, IOException, InvalidConfigurationException {
        loadContext.loadedFiles.add(file);

        YamlMap ctx = MenuFilePostprocessor.apply(MenuFilePreprocessor.loadFile(file, loader), loader.getLogger());
        List<MenuConfig> supers = load(FileUtil.findFiles(file, loader, ctx.get("extends").decode(YamlCodec.STRING_LIST, List.of())), loader, loadContext);
        String title = ctx.get("title").getAsString("title is not set!");
        @Nullable SpacedNameKey id = getId(ctx.get("id").getAsString(null), loader);
        @Nullable SpacedNameKey provider = getId(ctx.get("provider").getAsString(null), loader);

        InventoryType type = ctx.get("type").decode(BukkitYamlCodecs.INVENTORY_TYPE, InventoryType.CHEST);
        int size = ctx.get("size").getAsInt(54);

        List<SpacedNameKey> onlyOpenFrom = ctx.get("only-open-from", List.of()).stream().map(v -> getId(v.getAsString(), loader)).toList(); //todo rename

        Map<String, String> args = ctx.get("args").decode(MenuCodecs.ARGS_CODEC, Map.of());

        Map<String, MenuItemBuilder> items = ItemFactory.readItems(ctx.get("items").decode(YamlCodec.STRING_TO_YAML_MAP_MAP, Map.of()));

        Animator.AnimatorContext animator;
        if (ctx.has("animation")) {
            animator = AnimatorFactory.readAnimation(ctx.get("animation").decode(YamlCodec.YAML_MAP_LIST, List.of()), loader);
        } else {
            animator = null;
        }
        Map<String, Animator.AnimatorContext> animations = new HashMap<>();
        if (ctx.has("animations")) {
            Map<String, List<YamlMap>> animationsRaw = ctx.get("animations").decode(YamlCodec.mapOf(YamlCodec.STRING, YamlCodec.YAML_MAP_LIST), Map.of());
            for (var entry : animationsRaw.entrySet()) {
                animations.put(entry.getKey(), AnimatorFactory.readAnimation(entry.getValue(), loader));
            }
        }
        CommandList commandList = new CommandList(ctx.get("commands-list")); //todo rename
        Map<String, CommandRequirements> menuEventListeners =
                ctx.get("menu-events").getAsMap(YamlCodec.STRING, v -> {  //todo rename
                    YamlMap context = v.getAsYamlMap();
//                    Requirements requirements = ObjectUtil.mapIfNotNullOrDefault(
//                            context.get("requirements"),
//                            RequirementsFactory::read,
//                            Requirements.EMPTY
//                    );

                    return new CommandRequirements(
                            Requirements.EMPTY, //todo
                            context.get("deny_commands").decode(YamlCodec.STRINGS, List.of()),
                            context.get("commands").decode(YamlCodec.STRINGS, List.of())
                    );
                }, Map.of());
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
                commandList,
                menuEventListeners,
                animations,
                loadContext.loadedFiles
        );
    }*/

/*
    private SpacedNameKey getId(@Nullable String id, MenuLoader loader) {
        if (id == null) return null;
        if (id.contains(":")) {
            return new SpacedNameKey(id);
        } else {
            return new SpacedNameKey(loader.getPlugin().getName().toLowerCase(Locale.ROOT), id);
        }
    }*/

/*    private List<MenuConfig> load(List<File> files, MenuLoader loader, MenuLoadContext ctx) throws InvalidMenuConfigException, IOException, InvalidConfigurationException {
        if (files.isEmpty()) return Collections.emptyList();
        List<MenuConfig> result = new ArrayList<>();
        for (File file : files) {
            if (ctx.loadedFiles.contains(file)) continue;
            result.add(load0(file, loader, ctx));
        }
        return result;
    }*/
/*
    private static class MenuLoadContext {
        private final List<File> loadedFiles = new ArrayList<>();
    }*/
}
