package org.by1337.bmenu.factory;


import org.by1337.bmenu.MenuConfig;
import org.by1337.bmenu.MenuLoader;

import java.io.File;

public class MenuFactory {

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
}
