package dev.by1337.bmenu.factory;


import dev.by1337.yaml.codec.DataResult;
import dev.by1337.bmenu.MenuConfig;
import dev.by1337.bmenu.MenuLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MenuFactory {

    private static final Logger log = LoggerFactory.getLogger("BMenu");

    public static MenuConfig load(File file, MenuLoader loader) throws InvalidMenuConfigException {
        try {
            MenuCodec codec = new MenuCodec(file, loader);
            DataResult<MenuConfig> result = codec.decode();
            if (result.hasError() && result.hasResult()){
                log.error("Errors during decoding menu {}\n{}", file.getPath(), result.error());
                return result.result();
            }
            if (result.hasResult()){
                return result.result();
            }
            throw new InvalidMenuConfigException(result.error());
        } catch (InvalidMenuConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidMenuConfigException(e.getMessage(), e);
        }
    }
}
