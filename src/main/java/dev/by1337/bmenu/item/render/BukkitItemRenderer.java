package dev.by1337.bmenu.item.render;

import dev.by1337.core.util.text.minimessage.MiniMessage;
import dev.by1337.plc.Placeholderable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.item.ItemModel;
import dev.by1337.bmenu.text.RawTextComponent;
import dev.by1337.bmenu.text.SourcedComponentLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BukkitItemRenderer extends AbstractBukkitItemRenderer {

    @Override
    protected ItemStack applyDisplay(ItemStack itemStack, ItemModel item, Menu menu, Placeholderable placeholders) {
        ItemMeta im = itemStack.getItemMeta();
        var name = item.name();
        if (name != null) {
            im.displayName(toComponent(name, placeholders));
        }
        var lore = item.lore();
        if (lore != null) {
            List<Component> loreComponents = new ArrayList<>();
            for (SourcedComponentLike componentLike : lore) {
                applyComponent(componentLike, placeholders, loreComponents::add);
            }
            im.lore(loreComponents);
        }
        if (im instanceof Damageable damageable){
            damageable.setDamage(item.damage().getOrDefault(placeholders, 0));
        }
        itemStack.setItemMeta(im);
        itemStack.setAmount(item.amount().getOrDefault(placeholders, 1));
        return itemStack;
    }

    private void applyComponent(SourcedComponentLike c, Placeholderable placeholders, Consumer<Component> processor) {
        if (c instanceof RawTextComponent raw) {
            String s = placeholders.replace(raw.source());
            for (String line : s.split("\n")) {
                processor.accept(MiniMessage.deserialize(line));
            }
        } else {
            processor.accept(c.asComponent());
        }
    }

    private Component toComponent(SourcedComponentLike c, Placeholderable placeholders) {
        if (c instanceof RawTextComponent c1) {
            return c1.asComponent(placeholders);
        }
        return c.asComponent();
    }
}
