package org.by1337.bmenu.animation.impl;


import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.FrameOpcodes;
import org.by1337.bmenu.animation.util.AnimationUtil;
import org.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

public class CopyAnimOpcode implements FrameOpcode {
    public static final YamlCodec<CopyAnimOpcode> CODEC = YamlCodec.STRING.schema(SchemaTypes.STRING_OR_NUMBER)
            .map(CopyAnimOpcode::new, v -> AnimationUtil.slotsToString(v.src) + " " + AnimationUtil.slotsToString(v.dest));
    private final int[] src;
    private final int[] dest;

    public CopyAnimOpcode(String args) {
        int[][] pair = AnimationUtil.parsePairSlots(args);
        src = pair[0];
        dest = pair[1];
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        if (src.length == 0) {
            throw new IllegalArgumentException("src array is empty");
        }
        int srcIndex = 0;
        for (int toIndex : dest) {
            int fromIndex = src[srcIndex];

            if (fromIndex < 0 || fromIndex >= matrix.length || toIndex < 0 || toIndex >= matrix.length) {
                throw new IndexOutOfBoundsException("Индексы 'from' или 'to' выходят за пределы меню.");
            }
            matrix[toIndex] = matrix[fromIndex];
            if (srcIndex < src.length - 1) {
                srcIndex++;
            }
        }
    }

    public int[] getSrc() {
        return src;
    }

    public int[] getDest() {
        return dest;
    }

    @Override
    public @Nullable FrameOpcodes type() {
        return FrameOpcodes.COPY;
    }
}
