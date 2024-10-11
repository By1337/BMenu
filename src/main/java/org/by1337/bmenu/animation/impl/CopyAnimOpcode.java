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
        String[] args = ctx.getAsString().split(" ");

        Pair<int[], int[]> pair = AnimationUtil.parsePairSlots(ctx.getAsString().substring(args[0].length()));
        src = pair.getLeft();
        dest = pair.getRight();
    }

    @Override
    public void apply(MenuItem[] matrix, Menu menu, Animator animator) {
        if (src.length != dest.length) {
            throw new IllegalArgumentException("Количество слотов 'from' и 'to' должно совпадать.");
        }

        for (int i = 0; i < src.length; i++) {
            int fromIndex = src[i];
            int toIndex = dest[i];

            if (fromIndex < 0 || fromIndex >= matrix.length || toIndex < 0 || toIndex >= matrix.length) {
                throw new IndexOutOfBoundsException("Индексы 'from' или 'to' выходят за пределы массива.");
            }
            matrix[toIndex] = matrix[fromIndex];
        }
    }
}
