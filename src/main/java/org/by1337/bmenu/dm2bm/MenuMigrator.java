package org.by1337.bmenu.dm2bm;

import org.bukkit.configuration.InvalidConfigurationException;
import org.by1337.blib.configuration.YamlConfig;
import org.by1337.blib.configuration.YamlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class MenuMigrator {
    private static final Logger LOGGER = LoggerFactory.getLogger("BMenu#Migrator");

    public static void migrate(File sourceFile, File targetFile) throws IOException, InvalidConfigurationException {
        YamlConfig source = new YamlConfig(sourceFile);
        YamlConfig target = new YamlConfig(targetFile);
        StringBuilder header = new StringBuilder();

        target.set("provider", "default");
        target.set("id", "migrated:" + sourceFile.getName().split("\\.")[0]);
        mergeIfExist("menu_title", source, "title", target);
        mergeIfExist("inventory_type", source, "type", target);
        mergeIfExist("size", source, "size", target);
        if (source.has("update_interval")) {
            header.append("#update_interval: ").append(source.get("update_interval").getAsString()).append(" # такого параметра нет в BMenu так как он должен быть на каждом предмете отдельно").append("\n");
        }
        if (source.has("open_command")) {
            header.append("#open_command: ").append(source.get("open_command").getAsString()).append(" # такого параметра нет в BMenu так как он должен быть указан в config.yml").append("\n");
        }

    }

    private static void mergeIfExist(String path, YamlContext from, String resultPath, YamlContext to) {
        if (from.has(path)) to.set(path, from.get(resultPath));
    }

}
