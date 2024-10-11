package org.by1337.bmenu.factory;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.nbt.NBTParser;
import org.by1337.blib.nbt.impl.ListNBT;
import org.by1337.bmenu.MenuLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class MenuFilePreprocessor {

    public static YamlContext loadFile(File file, MenuLoader loader) throws InvalidMenuConfigException{
        String str = readYamlAndApplyPreprocessor(file, loader, new HashSet<>());
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.loadFromString(str);
        } catch (InvalidConfigurationException e) {
            throw new InvalidMenuConfigException("Не удалось прочитать файл так как в синтаксисе yaml ошибка! https://codebeautify.org/yaml-validator - здесь можно проверить конфиг на наличие ошибок в yaml", e);
        }
        return new YamlContext(configuration);
    }

    private static String readYamlAndApplyPreprocessor(File file, MenuLoader loader, Set<File> loaded) throws InvalidMenuConfigException {
        if (loaded.contains(file)) return "";
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
                            List<File> includes = FileUtil.findFiles(file, loader, filesList);
                            for (File includeFile : includes) {
                                sb.append(readYamlAndApplyPreprocessor(includeFile, loader, loaded));
                            }
                        } catch (Throwable t) {
                            throw new InvalidMenuConfigException(
                                    "в файле обнаружено include: я его прочитал но не получилось его проанализировать. Вы точно используете формат include: []?. то что я прочитал '{}'",
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
}
