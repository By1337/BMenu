package dev.by1337.bmenu.animation.impl;

import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import dev.by1337.bmenu.MenuItem;
import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.animation.FrameOpcode;
import dev.by1337.bmenu.animation.FrameOpcodes;
import dev.by1337.bmenu.animation.util.AnimationUtil;
import dev.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

public class CopyFromBaseAnimOpcode implements FrameOpcode {
    public static final YamlCodec<CopyFromBaseAnimOpcode> CODEC = YamlCodec.STRING.schema(SchemaTypes.STRING_OR_NUMBER)
            .map(CopyFromBaseAnimOpcode::new, v -> AnimationUtil.slotsToString(v.src) + " " + AnimationUtil.slotsToString(v.dest));
    private final int[] src;
    private final int[] dest;

    public CopyFromBaseAnimOpcode(String args) {
        int[][] pair = AnimationUtil.parsePairSlots(args);
        src = pair[0];
        dest = pair[1];
        if (src.length == 0) {
            throw new IllegalArgumentException("src array is empty");
        }
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        MenuItem[] base = menu.getMatrix();
        int srcIndex = 0;
        for (int toIndex : dest) {
            int fromIndex = src[srcIndex];

            if (fromIndex < 0 || fromIndex >= matrix.length || toIndex < 0 || toIndex >= matrix.length) {
                throw new IndexOutOfBoundsException("Индексы 'from' или 'to' выходят за пределы меню.");
            }
            matrix[toIndex] = base[fromIndex];
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
        return FrameOpcodes.COPY_FROM_BASE;
    }
}
