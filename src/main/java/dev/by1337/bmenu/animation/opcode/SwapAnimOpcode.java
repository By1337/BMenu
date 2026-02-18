package dev.by1337.bmenu.animation.opcode;

import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.animation.FrameOpcode;
import dev.by1337.bmenu.animation.FrameOpcodes;
import dev.by1337.bmenu.animation.util.AnimationUtil;
import dev.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

public class SwapAnimOpcode implements FrameOpcode {
    public static final YamlCodec<SwapAnimOpcode> CODEC = YamlCodec.STRING.schema(SchemaTypes.STRING_OR_NUMBER)
            .map(SwapAnimOpcode::new, v -> AnimationUtil.slotsToString(v.from) + " " + AnimationUtil.slotsToString(v.to));
    private final int[] from;
    private final int[] to;

    public SwapAnimOpcode(String args) {
        int[][] pair = AnimationUtil.parsePairSlots(args);
        from = pair[0];
        to = pair[1];
    }

    @Override
    public void apply(SlotContent[] matrix, Menu menu, Animator animator) {
        if (from.length != to.length) {
            throw new IllegalArgumentException("Количество слотов 'from' и 'to' должно совпадать.");
        }

        for (int i = 0; i < from.length; i++) {
            int fromIndex = from[i];
            int toIndex = to[i];

            if (fromIndex < 0 || fromIndex >= matrix.length || toIndex < 0 || toIndex >= matrix.length) {
                throw new IndexOutOfBoundsException("Индексы 'from' или 'to' выходят за пределы массива.");
            }
            matrix[toIndex] = matrix[fromIndex];
            matrix[fromIndex] = null;
        }
    }

    public int[] getFrom() {
        return from;
    }

    public int[] getTo() {
        return to;
    }

    @Override
    public @Nullable FrameOpcodes type() {
        return FrameOpcodes.SWAP;
    }
}
