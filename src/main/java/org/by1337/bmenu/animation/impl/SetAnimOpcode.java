package org.by1337.bmenu.animation.impl;


import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.MenuItemBuilder;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.FrameOpcodes;
import org.by1337.bmenu.animation.util.AnimationUtil;
import org.by1337.bmenu.hook.ItemStackCreator;
import org.jetbrains.annotations.Nullable;

public class SetAnimOpcode implements FrameOpcode {
    public static final YamlCodec<SetAnimOpcode> CODEC = YamlCodec.STRING.schema(SchemaTypes.STRING_OR_NUMBER)
            .map(SetAnimOpcode::new, v -> v.item + " " + AnimationUtil.slotsToString(v.slots));
    private final String item;
    private final int[] slots;

    public SetAnimOpcode(String ctx) {
        String[] args = ctx.split(" ");
        item = args[0];
        slots = AnimationUtil.readSlots(args[1]);
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        MenuItemBuilder builder = menu.getConfig().findMenuItem(menu.replace(item), menu);
        if (builder == null) {
            AnimationUtil.set(new MenuItem(slots, ItemStackCreator.getItem(menu.replace(item))), matrix, slots);
        } else {
            MenuItem menuItem1 = builder.build(menu);
            if (menuItem1 != null) {
                AnimationUtil.set(menuItem1, matrix, slots);
            }
        }
    }
    @Override
    public @Nullable FrameOpcodes type() {
        return FrameOpcodes.SET;
    }
}
