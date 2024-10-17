package org.by1337.bmenu.impl;

import org.bukkit.entity.Player;
import org.by1337.blib.command.CommandException;
import org.by1337.bmenu.Menu;
import org.by1337.bmenu.MenuConfig;
import org.by1337.bmenu.MenuItem;
import org.by1337.bmenu.animation.util.AnimationUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public abstract class MultiPageMenu<T> extends Menu {
    protected int currentPage = 0;
    protected int maxPage = 0;
    protected int lastPage = -1;
    protected int[] itemSlots;

    public MultiPageMenu(MenuConfig config, Player viewer, @Nullable Menu previousMenu) {
        super(config, viewer, previousMenu);
        itemSlots = AnimationUtil.readSlots("0-9");
        registerPlaceholder("{max_page}", () -> maxPage == 0 ? 1 : maxPage);
        registerPlaceholder("{current_page}", () -> currentPage + 1);
    }

    protected abstract List<T> getItems();

    protected abstract Function<T, MenuItem> getItemFunction();

    @Override
    protected void generate() {
        List<T> items = getItems();
        maxPage = (int) Math.ceil((double) items.size() / itemSlots.length);
        if (currentPage > maxPage) {
            currentPage = maxPage - 1;
            if (currentPage < 0) currentPage = 0;
        }
        if (currentPage * itemSlots.length >= items.size()) {
            maxPage = 0;
        }

        Iterator<Integer> slotsIterator = new SlotsIterator();
        for (int x = currentPage * itemSlots.length; x < items.size(); x++) {
            T item = items.get(x);
            if (slotsIterator.hasNext()) {
                MenuItem menuItem = getItemFunction().apply(item);
                if (menuItem == null) continue;
                menuItem.setSlots(new int[]{slotsIterator.next()});
                setItem(menuItem);
            } else {
                break;
            }
        }
    }

    @Override
    protected boolean runCommand(String cmd) throws CommandException {
        return switch (cmd) {
            case "[NEXT_PAGE]" -> {
                if (currentPage < maxPage - 1) {
                    currentPage++;
                }
                yield true;
            }
            case "[PREVIOUS_PAGE]" -> {
                if (currentPage > 0) {
                    currentPage--;
                }
                yield true;
            }
            default -> false;
        };
    }

    protected class SlotsIterator implements Iterator<Integer> {
        private int x = 0;

        @Override
        public boolean hasNext() {
            return itemSlots != null && x < itemSlots.length;
        }

        @Override
        public Integer next() {
            return itemSlots[x++];
        }
    }
}
