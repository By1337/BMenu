package dev.by1337.bmenu.animation.opcode;

import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.animation.FrameOpcode;
import dev.by1337.bmenu.animation.FrameOpcodes;
import dev.by1337.bmenu.animation.util.AnimationUtil;
import org.jetbrains.annotations.Nullable;

public class RemoveIfNotEmptyAnimOpcode implements FrameOpcode {
    public static final YamlCodec<RemoveIfNotEmptyAnimOpcode> CODEC = YamlCodec.STRING.schema(SchemaTypes.STRING_OR_NUMBER)
            .map(RemoveIfNotEmptyAnimOpcode::new, v -> AnimationUtil.slotsToString(v.slots));
    private final int[] slots;

    public RemoveIfNotEmptyAnimOpcode(String ctx) {
        slots = AnimationUtil.readSlots(ctx);
    }

    @Override
    public void apply(SlotContent[] matrix, Menu menu, Animator animator) {
        for (int slot : slots) {
            var item = menu.matrix()[slot];
            if (item != null/* && !item.getItemStack().getType().isAir()*/) { //todo?
                matrix[slot] = null;
            }
        }
    }
    @Override
    public @Nullable FrameOpcodes type() {
        return FrameOpcodes.REMOVE_IF_NOT_EMPTY;
    }
}
