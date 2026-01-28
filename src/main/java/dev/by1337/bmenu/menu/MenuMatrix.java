package dev.by1337.bmenu.menu;

import dev.by1337.bmenu.slot.SlotContent;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class MenuMatrix {
    private SlotContent[][] matrices;
    private final int size;
    private SlotContent[] baseLayer;
    private SlotContent[] animationLayer;
    private final Set<SlotContent> ticked = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Menu menu;

    public MenuMatrix(int size, Menu menu) {
        this.size = size;
        this.menu = menu;
        this.matrices = new SlotContent[3][];
        baseLayer = getMatrix(0);
        animationLayer = getMatrix(1);
    }

    public SlotContent getItemInSlot(int slot) {
        if (slot >= size || slot < 0) return null;
        for (int idx = matrices.length - 1; idx >= 0; idx--) {
            var layer = matrices[idx];
            if (layer != null) {
                var item = layer[slot];
                if (item != null) return item;
            }
        }
        return null;
    }

    public void clear() {
        this.matrices = new SlotContent[3][];
        baseLayer = getMatrix(0);
        animationLayer = getMatrix(1);
    }

    public void flushTo(SlotContent[] matrix) {
        Arrays.fill(matrix, null);
        for (SlotContent[] layer : matrices) {
            if (layer == null) continue;
            for (int slot = 0; slot < size; slot++) {
                var item = layer[slot];
                if (item != null && item.isVisible(menu)) {
                    matrix[slot] = item;
                }
            }
        }
    }

    public void doTick() { //todo tick list?
        ticked.clear();
        for (SlotContent[] layer : matrices) {
            if (layer == null) continue;
            for (int slot = 0; slot < size; slot++) {
                SlotContent item = layer[slot];
                if (item == null) continue;
                if (item.isTicking() && ticked.add(item))
                    item.doTick(menu);
                if (item.isRemoved())
                    layer[slot] = null;
            }
        }
    }

    public SlotContent[] getBaseLayer() {
        return baseLayer;
    }

    public SlotContent[] getAnimationLayer() {
        return animationLayer;
    }

    public SlotContent[] getMatrix(int idx) {
        if (idx > 128) throw new IllegalArgumentException("Index out of range " + idx);
        ensureCapacity(idx + 1);
        var v = matrices[idx];
        if (v == null) return matrices[idx] = new SlotContent[size];
        return v;
    }


    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= matrices.length) return;
        int newCap = Math.max(matrices.length + 1, minCapacity);
        matrices = Arrays.copyOf(matrices, newCap);
    }
}
