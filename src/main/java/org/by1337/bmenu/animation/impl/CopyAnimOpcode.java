package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlValue;
import org.by1337.blib.util.Pair;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.util.AnimationUtil;

public class CopyAnimOpcode implements FrameOpcode {
    private final int[] src;
    private final int[] dest;

    public CopyAnimOpcode(YamlValue ctx) {
        String args = ctx.getAsString();

        Pair<int[], int[]> pair = AnimationUtil.parsePairSlots(args);
        src = pair.getLeft();
        dest = pair.getRight();
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
}
