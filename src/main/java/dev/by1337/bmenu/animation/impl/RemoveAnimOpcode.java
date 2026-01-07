package dev.by1337.bmenu.animation.impl;

import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.item.MenuItem;
import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.animation.FrameOpcode;
import dev.by1337.bmenu.animation.FrameOpcodes;
import dev.by1337.bmenu.animation.util.AnimationUtil;
import org.jetbrains.annotations.Nullable;

public class RemoveAnimOpcode implements FrameOpcode {
    public static final YamlCodec<RemoveAnimOpcode> CODEC = YamlCodec.STRING.schema(SchemaTypes.STRING_OR_NUMBER)
            .map(RemoveAnimOpcode::new, v -> AnimationUtil.slotsToString(v.slots));
    private final int[] slots;

    public RemoveAnimOpcode(String ctx) {
        slots = AnimationUtil.readSlots(ctx);
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        AnimationUtil.set(null, matrix, slots);
    }
    @Override
    public @Nullable FrameOpcodes type() {
        return FrameOpcodes.REMOVE;
    }
}
