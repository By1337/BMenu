package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlValue;
import org.by1337.blib.util.Pair;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.util.AnimationUtil;

public class CopyFromBaseAnimOpcode implements FrameOpcode {
    private final int[] src;
    private final int[] dest;

    public CopyFromBaseAnimOpcode(YamlValue ctx) {
        String args = ctx.getAsString();

        Pair<int[], int[]> pair = AnimationUtil.parsePairSlots(args);
        src = pair.getLeft();
        dest = pair.getRight();
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
}
