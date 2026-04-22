package dev.by1337.bmenu.factory;

import dev.by1337.bmenu.loader.MenuLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUtil {
    public static File findFile(File base, MenuLoader loader, String path) {
        var l = findFiles(base, loader, List.of(path));
        if (l.isEmpty())
            throw new InvalidMenuConfigException("Invalid menu path: {}", path);
        return l.get(0);
    }
    public static List<File> findFiles(File base, MenuLoader loader, List<String> files) {
        if (files.isEmpty()) return Collections.emptyList();
        File fileFolder = base.getParentFile();
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
                File menu = new File(loader.homeDir(), s.substring(1));
                if (!menu.exists()) {
                    throw new InvalidMenuConfigException("Invalid menu path: {}", menu.getAbsolutePath());
                }
                result.add(menu);
            }
        }
        return result;
    }
}
