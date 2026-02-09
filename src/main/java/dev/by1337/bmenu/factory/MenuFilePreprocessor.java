package dev.by1337.bmenu.factory;

import dev.by1337.bmenu.loader.MenuLoader;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.YamlCodec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class MenuFilePreprocessor {

    public static String loadFile(File file, MenuLoader loader) throws InvalidMenuConfigException {
        return readYamlAndApplyPreprocessor(file, loader, new HashSet<>());
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
                    StringBuilder include = new StringBuilder(line);
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
                            YamlMap yamlMap = YamlMap.loadFromString(include.toString());
                            List<String> filesList = yamlMap.get("include").decode(YamlCodec.STRINGS).getOrThrow();
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
