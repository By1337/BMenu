package dev.by1337.bmenu.menu;

import dev.by1337.bmenu.item.MenuItem;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class MenuMatrix {
    private MenuItem[][] matrices;
    private final int size;
    private final MenuItem[] baseLayer;
    private final MenuItem[] animationLayer;
    private final Set<MenuItem> ticked = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Menu menu;

    public MenuMatrix(int size, Menu menu) {
        this.size = size;
        this.menu = menu;
        this.matrices = new MenuItem[3][];
        baseLayer = getMatrix(0);
        animationLayer = getMatrix(1);
    }

    public MenuItem getItemInSlot(int slot) {
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
        this.matrices = new MenuItem[3][];
        Arrays.fill(baseLayer, null);
        Arrays.fill(animationLayer, null);
        matrices[0] = baseLayer;
        matrices[1] = animationLayer;
    }

    public void flushTo(MenuItem[] matrix) {
        Arrays.fill(matrix, null);
        for (MenuItem[] layer : matrices) {
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
        for (MenuItem[] layer : matrices) {
            if (layer == null) continue;
            for (int slot = 0; slot < size; slot++) {
                MenuItem item = layer[slot];
                if (item == null) continue;
                if (item.isTicking() && ticked.add(item))
                    item.doTick(menu);
                if (item.isDie())
                    layer[slot] = null;
            }
        }
    }

    public MenuItem[] getBaseLayer() {
        return baseLayer;
    }

    public MenuItem[] getAnimationLayer() {
        return animationLayer;
    }

    public MenuItem[] getMatrix(int idx) {
        if (idx > 128) throw new IllegalArgumentException("Index out of range " + idx);
        ensureCapacity(idx + 1);
        var v = matrices[idx];
        if (v == null) return matrices[idx] = new MenuItem[size];
        return v;
    }


    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= matrices.length) return;
        int newCap = Math.max(matrices.length + 1, minCapacity);
        matrices = Arrays.copyOf(matrices, newCap);
    }
}
