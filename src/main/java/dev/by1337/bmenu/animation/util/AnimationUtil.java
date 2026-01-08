package dev.by1337.bmenu.animation.util;

import dev.by1337.bmenu.item.SlotContent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AnimationUtil {

    public static int[][] parsePairSlots(String input) {
        int[] src;
        int[] dest;
        String[] args = input.split(" ");
        if (args[0].endsWith("++")) {
            int start = Integer.parseInt(args[0].substring(0, args[0].length() - 2));
            src = new int[]{start};
            dest = new int[]{start + 1};
        } else if (args[0].endsWith("--")) {
            int start = Integer.parseInt(args[0].substring(0, args[0].length() - 2));
            src = new int[]{start};
            dest = new int[]{start - 1};
        } else {
            src = readSlots(args[0]);
            if (args.length != 1) {
                dest = readSlots(args[1]);
            } else {
                dest = src;
            }
        }
        return new int[][]{src, dest};
    }

    public static int[] readSlots(String str) {
        if (str.contains(",")) {
            List<Integer> result = new ArrayList<>();
            String[] slots = str.split(",");
            for (String slot : slots) {
                int[] arr = readSlots0(slot);
                for (int i : arr) {
                    result.add(i);
                }
            }
            int[] resultArray = new int[result.size()];
            for (int i = 0; i < result.size(); i++) {
                resultArray[i] = result.get(i);
            }
            return resultArray;
        }
        return readSlots0(str);
    }

    private static int[] readSlots0(String str) {
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

    public static void set(@Nullable SlotContent who, SlotContent[] to, int[] inSlots) {
        for (int inSlot : inSlots) {
            to[inSlot] = who;
        }
    }

    public static String slotsToString(int[] arr) {
        if (arr.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i : arr) {
            sb.append(i).append(",");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

}
