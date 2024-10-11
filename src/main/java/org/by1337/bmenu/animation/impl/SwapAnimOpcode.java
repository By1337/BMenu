package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlValue;
import org.by1337.blib.util.Pair;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.util.AnimationUtil;

public class SwapAnimOpcode implements FrameOpcode {
    private final int[] from;
    private final int[] to;

    public SwapAnimOpcode(YamlValue ctx) {
        String[] args = ctx.getAsString().split(" ");

        Pair<int[], int[]> pair = AnimationUtil.parsePairSlots(ctx.getAsString().substring(args[0].length()));
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
}
