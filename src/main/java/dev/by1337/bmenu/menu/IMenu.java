package dev.by1337.bmenu.menu;

import dev.by1337.bmenu.MenuConfig;
import dev.by1337.bmenu.MenuLoader;
import dev.by1337.bmenu.command.CommandRunner;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.item.SlotContent;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.plc.PlaceholderResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMenu extends InventoryHolder, CommandRunner<ExecuteContext>, PlaceholderApplier {

    default void open() {
        open(false);
    }

    void open(boolean isReopen);

    void tick();

    void reopen();

    void refresh();

    void close();

    void setItem(SlotContent item, int slot);

    void setItem(SlotContent item, int slot, SlotContent[] matrix);

    void onClose(InventoryCloseEvent event);

    void onClick(InventoryDragEvent e);

    void onClick(InventoryClickEvent e);

    SlotContent findItemInSlot(int slot);

    void onEvent(String event);

    MenuLoader getLoader();

    Player getViewer();

    SlotContent[] getMatrix();

    SlotContent[] getAnimationMask();

    MenuConfig getConfig();

    @Nullable IMenu getPreviousMenu();

    @Nullable SlotContent getLastClickedItem();

    default @Nullable Object getLastClickedItemData() {
        var v = getLastClickedItem();
        return v == null ? null : v.getPayload();
    }
    void addArgument(String key, String value);
    boolean isSupportsHotReload();
    void onHotReload(@NotNull IMenu oldMenu);
    SlotContent[] getLayer(int index);
    PlaceholderResolver<IMenu> getPlaceholderResolver();

}
