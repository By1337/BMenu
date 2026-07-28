package dev.by1337.bmenu.loader.v2;

import dev.by1337.bmenu.loader.MenuLoader;

import java.util.function.Function;

public class MenuDecoders {
    private static Function<MenuLoader, MenuDecoder> factory = MenuDecoderImpl::new;

    public static Function<MenuLoader, MenuDecoder> factory() {
        return factory;
    }

    public static void setFactory(Function<MenuLoader, MenuDecoder> factory) {
        MenuDecoders.factory = factory;
    }
}
