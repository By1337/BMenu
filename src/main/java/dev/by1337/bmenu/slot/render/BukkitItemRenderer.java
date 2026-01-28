package dev.by1337.bmenu.slot.render;

import dev.by1337.item.ItemModel;
import dev.by1337.item.ItemComponents;
import dev.by1337.bmenu.util.holder.IntHolder;
import dev.by1337.core.util.text.minimessage.MiniMessage;
import dev.by1337.plc.PlaceholderApplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.text.RawTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BukkitItemRenderer extends AbstractBukkitItemRenderer {

    @Override
    protected ItemStack applyDisplay(ItemStack itemStack, ItemModel item, Menu menu, PlaceholderApplier placeholders) {
        ItemMeta im = itemStack.getItemMeta();

        var name = item.get(ItemComponents.NAME);
        if (name != null) {
            im.displayName(toComponent(name, placeholders));
        }
        var lore = item.get(ItemComponents.LORE);
        if (lore != null) {
            List<Component> loreComponents = new ArrayList<>();
            lore.forEachLore(line ->
                    applyComponent(line, placeholders, loreComponents::add));
            im.lore(loreComponents);
        }
        if (im instanceof Damageable damageable){
            damageable.setDamage(item.get(ItemComponents.DAMAGE, IntHolder.ZERO).getOrDefault(placeholders, 0));
        }
        itemStack.setItemMeta(im);
        itemStack.setAmount(item.get(ItemComponents.AMOUNT, IntHolder.ONE).getOrDefault(placeholders, 1));
        return itemStack;
    }

    private void applyComponent(ComponentLike c, PlaceholderApplier placeholders, Consumer<Component> processor) {
        if (c instanceof RawTextComponent raw) {
            String s = placeholders.setPlaceholders(raw.source());
            for (String line : s.split("\n")) {
                processor.accept(MiniMessage.deserialize(line).decoration(TextDecoration.ITALIC, false));
            }
        } else {
            processor.accept(c.asComponent().decoration(TextDecoration.ITALIC, false));
        }
    }

    private Component toComponent(ComponentLike c, PlaceholderApplier placeholders) {
        if (c instanceof RawTextComponent c1) {
            return c1.asComponent(placeholders).decoration(TextDecoration.ITALIC, false);
        }
        return c.asComponent().decoration(TextDecoration.ITALIC, false);
    }
}
