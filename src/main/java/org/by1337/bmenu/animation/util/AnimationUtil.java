package org.by1337.bmenu.animation.util;

import org.by1337.bmenu.MenuItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AnimationUtil {

    public static int[] readSlots(String str) {
        if (str.contains("-")) {
            List<Integer> slots = new ArrayList<>();
            String[] s = str.replace(" ", "").split("-");
            int x = Integer.parseInt(s[0]);
            int x1 = Integer.parseInt(s[1]);
            for (int i = Math.min(x, x1); i <= Math.max(x, x1); i++) {
                slots.add(i);
            }
            int[] slot = new int[slots.size()];
            for (int i = 0; i < slots.size(); i++) {
                slot[i] = slots.get(i);
            }
            return slot;
        } else {
            return new int[]{Integer.parseInt(str)};
        }
    }
    public static void set(@Nullable MenuItem who, MenuItem[] to, int[] inSlots){
        for (int inSlot : inSlots) {
            to[inSlot] = who;
        }
    }

}
