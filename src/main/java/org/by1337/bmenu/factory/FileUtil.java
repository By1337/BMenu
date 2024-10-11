package org.by1337.bmenu.factory;

import org.by1337.bmenu.MenuLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUtil {
    public static List<File> findFiles(File file, MenuLoader loader, List<String> files) throws InvalidMenuConfigException {
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
}
