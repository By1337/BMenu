package dev.by1337.bmenu.menu;

import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.command.CommandRunner;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.loader.MenuConfig;
import dev.by1337.bmenu.loader.MenuLoader;
import dev.by1337.bmenu.placeholder.PlaceholderResolverList;
import dev.by1337.bmenu.slot.SlotBuilderSource;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.plc.PlaceholderResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

public interface Menu extends InventoryHolder, CommandRunner<ExecuteContext>, PlaceholderApplier, SlotBuilderSource {

    default void open() {
        open(false);
    }

    default void reopen() {
        open(true);
    }

    void open(boolean isReopen);

    void tick();

    void refresh();

    default void rebuildItemsInSlots(int[] slots) {
        for (int slot : slots) {
            SlotContent item;
            if ((item = findItemInSlot(slot)) != null) {
                item.setDirty(true);
            }
        }
    }

    void close();

    void flush();

    void onClose(InventoryCloseEvent event);

    void onClick(InventoryDragEvent e);

    void onClick(InventoryClickEvent e);

    SlotContent findItemInSlot(int slot);

    void setTitle(String title);

    void onEvent(String event);

    PlaceholderResolverList resolvers();

    SlotContent[] matrix();

    SlotContent[] animationMask();

    @Nullable SlotContent lastClickedItem();

    long lastClickTime();

    void addArgument(String key, String value);

    void addPlaceholderResolver(PlaceholderResolver<Menu> resolver);

    NamespacedKey getId();

    @Nullable Menu upperMenu();

    void setUpperMenu(@Nullable Menu upperMenu);

    @Nullable Menu previousMenu();

    MenuMatrix layers();

    MenuConfig config();

    MenuLoader loader();

    Player viewer();

    Animator animator();

    void setAnimator(Animator animator);

    int lastClickedSlot();

    default void setItem(SlotContent item, int slot) {
        setItem(item, slot, matrix());
    }

    default void setItem(SlotContent item, int slot, SlotContent[] matrix) {
        if (slot == -1) return;
        if (slot < 0 || matrix.length < slot) {
            loader().logger().error("Slot {} is out of bounds! Menu: {}", slot, getId());
        } else {
            matrix[slot] = item;
        }
    }
}
