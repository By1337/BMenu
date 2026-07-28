package dev.by1337.bmenu.loader.v2;

import dev.by1337.bmenu.loader.MenuConfig;
import dev.by1337.bmenu.loader.MenuLoader;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.codec.DataResult;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface MenuDecoder {
    DataResult<? extends MenuConfig> decode(File file);
    @Nullable MenuConfig appendFile(File file);
    MenuLoader loader();
    void setPlaceholders(PlaceholderApplier placeholders);
}
