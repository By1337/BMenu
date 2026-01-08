package dev.by1337.bmenu.item;

import dev.by1337.bmenu.click.ClickMap;
import dev.by1337.bmenu.click.MenuClickType;
import dev.by1337.bmenu.factory.fixer.ItemFixer;
import dev.by1337.bmenu.item.item.ItemModel;
import dev.by1337.bmenu.item.item.ItemModelImpl;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.plc.Placeholderable;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class SlotVariant {
    public static final YamlCodec<SlotVariant> CODEC = RecordYamlCodecBuilder.mapOf(
            SlotVariant::new,
            ItemModelImpl.CODEC.fieldOf(null, SlotVariant::itemModelImplOrNull, ItemModelImpl.AIR),
            ViewRequirement.CODEC.fieldOf("on_view", SlotVariant::viewRequirement,ViewRequirement.EMPTY),
            ClickMap.CODEC.fieldOf(null, SlotVariant::clicks, ClickMap.EMPTY),
            SlotTicker.CODEC.fieldOf("on_tick", SlotVariant::itemTicker)
    );
    private final ItemModel itemModel;
    private final ViewRequirement viewRequirement;
    private final ClickMap clicks;
    private final @Nullable SlotTicker itemTicker;

    public SlotVariant(ItemModel itemModel, ViewRequirement viewRequirement, ClickMap clicks, @Nullable SlotTicker itemTicker) {
        this.itemModel = itemModel;
        this.viewRequirement = viewRequirement;
        this.clicks = clicks;
        this.itemTicker = itemTicker;
    }
    public SlotVariant and(ItemModel itemModel){
        return new SlotVariant(
                this.itemModel.and(itemModel),
                viewRequirement,
                clicks,
                itemTicker
        );
    }

    public static SlotVariant of(ItemModel itemModel){
        return new SlotVariant(
                itemModel,
                ViewRequirement.EMPTY,
                ClickMap.EMPTY,
                null
        );
    }

    public static SlotVariant of(ItemModel itemModel, ClickMap clicks){
        return new SlotVariant(
                itemModel,
                ViewRequirement.EMPTY,
                clicks,
                null
        );
    }
    public boolean doClick(Menu menu, Player player, MenuClickType type, SlotContent item) {
        return clicks.doClick(menu, player, type, item.getPlaceholders(menu), item);
    }

    public ItemModelImpl itemModelImplOrNull() {
        return itemModel instanceof ItemModelImpl ? (ItemModelImpl) itemModel : null;
    }

    public ItemModel itemModel() {
        return itemModel;
    }

    public ViewRequirement viewRequirement() {
        return viewRequirement;
    }

    public ClickMap clicks() {
        return clicks;
    }

    public @Nullable SlotTicker itemTicker() {
        return itemTicker;
    }
}
