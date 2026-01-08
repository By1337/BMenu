package dev.by1337.bmenu.item;

import dev.by1337.bmenu.click.MenuClickType;
import dev.by1337.bmenu.item.item.ItemModel;
import dev.by1337.bmenu.item.slot.SimpleSlotContent;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.plc.Placeholderable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface SlotContent extends PlaceholderResolver<Menu> {

    static SlotContent ofMaterial(String material) {
        return new SimpleSlotContent(
                ItemModel.ofMaterial(material)
        );
    }

    Placeholderable getPlaceholders(Menu menu);

    void doClick(Menu menu, Player player, MenuClickType type);

    boolean isTicking();

    @Nullable Object getPayload();

    void setPayload(@Nullable Object data);

    void doTick(Menu menu);

    void markRemoved();

    boolean isRemoved();

    void setPlaceholder(String placeholder, Supplier<Object> value);

    boolean isDirty();

    void setDirty(boolean dirty);

    ItemModel getItemModel();

    boolean isVisible(Menu menu);

}
