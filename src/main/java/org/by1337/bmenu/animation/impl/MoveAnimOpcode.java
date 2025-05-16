package org.by1337.bmenu.animation.impl;

import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.by1337.blib.util.Pair;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.FrameOpcodes;
import org.by1337.bmenu.animation.util.AnimationUtil;
import org.jetbrains.annotations.Nullable;

public class MoveAnimOpcode implements FrameOpcode {
    public static final YamlCodec<MoveAnimOpcode> CODEC = YamlCodec.STRING.schema(SchemaTypes.STRING_OR_NUMBER)
            .map(MoveAnimOpcode::new, v -> AnimationUtil.slotsToString(v.from) + " " + AnimationUtil.slotsToString(v.to));
    private final int[] from;
    private final int[] to;

    public MoveAnimOpcode(String args) {
        Pair<int[], int[]> pair = AnimationUtil.parsePairSlots(args);
        from = pair.getLeft();
        to = pair.getRight();
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
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
        return FrameOpcodes.MOVE;
    }
}
