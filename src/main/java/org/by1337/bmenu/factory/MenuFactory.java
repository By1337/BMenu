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

        YamlContext ctx = loadFile(file, loader);
        List<MenuConfig> supers = load(findFiles(file, loader, ctx.getList("extends", String.class, Collections.emptyList())), loader, loadContext);
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

    private YamlContext loadFile(File file, MenuLoader loader) throws InvalidMenuConfigException {
        String str = readYamlAndApplyPreprocessor(file, loader);
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.loadFromString(str);
        } catch (InvalidConfigurationException e) {
            throw new InvalidMenuConfigException("Не удалось прочитать файл так как в синтаксисе yaml ошибка! https://codebeautify.org/yaml-validator - здесь можно проверить конфиг на наличие ошибок в yaml", e);
        }
        return new YamlContext(configuration);
    }

    private String readYamlAndApplyPreprocessor(File file, MenuLoader loader) throws InvalidMenuConfigException {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            StringBuilder sb = new StringBuilder();
            var iterator = lines.iterator();
            while (iterator.hasNext()) {
                String line = iterator.next();
                if (line.startsWith("#")) continue;
                if (line.trim().startsWith("include:")) {
                    StringBuilder include = new StringBuilder(line.replace("include:", ""));
                    boolean closed = false;
                    while (iterator.hasNext()) {
                        line = iterator.next();
                        include.append(line);
                        if (line.contains("]")) {
                            closed = true;
                            break;
                        }
                    }
                    if (!closed) {
                        throw new InvalidMenuConfigException(
                                "в файле обнаружено include: но он не был закрыт. то что я прочитал '{}'",
                                include.toString().replace("\n", "\\n")
                        );
                    } else {
                        try {
                            ListNBT listNBT = (ListNBT) NBTParser.parseList(include.toString());
                            List<String> filesList = listNBT.stream().map(n -> String.valueOf(n.getAsObject())).toList();
                            List<File> includes = findFiles(file, loader, filesList);
                            for (File includeFile : includes) {
                                sb.append(readYamlAndApplyPreprocessor(includeFile, loader));
                            }
                        } catch (Throwable t) {
                            throw new InvalidMenuConfigException(
                                    "в файле обнаружено include: я его прочитал но не получилось его проанализировать. Вы точно используете формат include: []?. то что я прочитал {}",
                                    include.toString().replace("\n", "\\n"),
                                    t
                            );
                        }
                    }
                } else {
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
