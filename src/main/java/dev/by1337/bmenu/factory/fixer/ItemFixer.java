package dev.by1337.bmenu.factory.fixer;

import dev.by1337.bmenu.compat.LegacyItemNormalizer;
import dev.by1337.yaml.YamlMap;
import org.jetbrains.annotations.Nullable;

/** Retained for already compiled integrations. */
@Deprecated
public class ItemFixer {
    public static void fixItem(YamlMap map) {
        LegacyItemNormalizer.fixItem(map);
    }

    public static void fixItem(YamlMap map, @Nullable YamlMap superItem) {
        LegacyItemNormalizer.fixItem(map, superItem);
    }
}
