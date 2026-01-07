package dev.by1337.bmenu.animation.impl;

import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.item.MenuItem;
import dev.by1337.bmenu.item.MenuItemBuilder;
import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.animation.FrameOpcode;
import dev.by1337.bmenu.animation.FrameOpcodes;
import dev.by1337.bmenu.animation.util.AnimationUtil;
import org.jetbrains.annotations.Nullable;

public class SetIfEmptyAnimOpcode implements FrameOpcode {
    public static final YamlCodec<SetIfEmptyAnimOpcode> CODEC = YamlCodec.STRING.schema(SchemaTypes.STRING_OR_NUMBER)
            .map(SetIfEmptyAnimOpcode::new, v -> v.item + " " + AnimationUtil.slotsToString(v.slots));
    private final String item;
    private final int[] slots;

    public SetIfEmptyAnimOpcode(String ctx) {
        String[] args = ctx.split(" ");
        item = args[0];
        slots = AnimationUtil.readSlots(args[1]);
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        MenuItemBuilder builder = menu.getConfig().findMenuItem(menu.replace(item), menu);
        if (builder == null) {

            setIfEmpty(MenuItem.ofMaterial(menu.replace(item)), matrix, slots);
        } else {
            MenuItem menuItem1 = builder.build(menu);
            if (menuItem1 != null) {
                setIfEmpty(menuItem1, matrix, slots);
            }
        }
    }
    public static void setIfEmpty(@Nullable MenuItem who, MenuItem[] to, int[] inSlots) {
        for (int inSlot : inSlots) {
            if (to[inSlot] == null){
                to[inSlot] = who;
            }
        }
    }
    @Override
    public @Nullable FrameOpcodes type() {
        return FrameOpcodes.SET_IF_EMPTY;
    }
}
