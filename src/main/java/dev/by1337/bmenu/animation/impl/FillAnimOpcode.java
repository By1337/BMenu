package dev.by1337.bmenu.animation.impl;

import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.MenuItem;
import dev.by1337.bmenu.MenuItemBuilder;
import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.animation.FrameOpcode;
import dev.by1337.bmenu.animation.FrameOpcodes;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class FillAnimOpcode implements FrameOpcode {
    public static final YamlCodec<FillAnimOpcode> CODEC = YamlCodec.STRING.schema(SchemaTypes.STRING_OR_NUMBER)
            .map(FillAnimOpcode::new, v -> v.item);
    private static final int[] EMPTY_ARRAY = new int[0];
    private final String item;

    public FillAnimOpcode(String ctx) {
        item = ctx;
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        MenuItemBuilder builder = menu.getConfig().findMenuItem(menu.replace(item), menu);
        MenuItem menuItem;
        if (builder == null) {
            menuItem = MenuItem.ofMaterial(menu.replace(item));
        } else {
            menuItem = builder.build(menu);
        }
        Arrays.fill(matrix, menuItem);
    }
    @Override
    public @Nullable FrameOpcodes type() {
        return FrameOpcodes.FILL;
    }
}
