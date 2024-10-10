package org.by1337.bmenu.animation.impl;

import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.util.AnimationUtil;

public class MoveAnimOpcode implements FrameOpcode {
    private final int[] from;
    private final int[] to;

    public MoveAnimOpcode(YamlValue ctx) {
        String[] args = ctx.getAsString().split(" ");

        if (args[0].contains("-")) {
            this.from = AnimationUtil.readSlots(args[0]);
            this.to = AnimationUtil.readSlots(args[1]);
        } else if (args[0].endsWith("++")) {
            int start = Integer.parseInt(args[0].substring(0, args[0].length() - 2));
            this.from = new int[]{start};
            this.to = new int[]{start + 1};
        } else if (args[0].endsWith("--")) {
            int start = Integer.parseInt(args[0].substring(0, args[0].length() - 2));
            this.from = new int[]{start};
            this.to = new int[]{start - 1};
        } else {
            this.from = new int[]{Integer.parseInt(args[0])};
            this.to = new int[]{Integer.parseInt(args[1])};
        }
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
