/*
package dev.by1337.bmenu.slot.render;

import dev.by1337.bmenu.hook.ItemStackCreator;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.util.ObjectUtil;
import dev.by1337.core.util.text.component.RawTextComponent;
import dev.by1337.item.component.impl.MaterialComponent;
import dev.by1337.item.util.IntHolder;
import dev.by1337.core.BCore;
import dev.by1337.core.ServerVersion;
import dev.by1337.core.bridge.inventory.InventoryUtil;
import dev.by1337.core.util.text.minimessage.MiniMessage;
import dev.by1337.item.ItemComponents;
import dev.by1337.item.ItemModel;
import dev.by1337.item.component.impl.ContainerComponent;
import dev.by1337.plc.PlaceholderApplier;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Arrow;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractBukkitItemRenderer implements ItemRenderer<Inventory> {
    private static final InventoryUtil INV_UTIL = BCore.getInventoryUtil();
    private static final ItemStack AIR = new ItemStack(Material.AIR);
    private static final Logger log = LoggerFactory.getLogger("BMenu");

    @Override
    public void render(Inventory ctx, int slot, ItemModel item, Menu menu, PlaceholderApplier placeholders) {
        // log.info("{} update slot {}", menu.getConfig().getId(), slot);
        if (item == null) {
            ctx.setItem(slot, AIR);
            return;
        }

        ItemStack itemStack = build(item, placeholders);
        ctx.setItem(slot, itemStack);
    }

    protected abstract ItemStack applyDisplay(ItemStack itemStack, ItemModel item, Menu menu, PlaceholderApplier placeholders);

    @Override
    public void flush(Inventory ctx, Menu menu) {
        INV_UTIL.flushInv(menu.getViewer());
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

    private ItemStack build(ItemModel item, PlaceholderApplier placeholders) {
        String material = item.get(ItemComponents.MATERIAL, MaterialComponent.DEFAULT).get(placeholders);
        ItemStack itemStack = ItemStackCreator.getItem(material);

        ItemMeta im = itemStack.getItemMeta();
        if (im == null) {
            return AIR;
        }
        var hide = item.get(ItemComponents.HIDE_FLAGS);
        if (hide != null) {
            hide.flags().forEach(im::addItemFlags);
            if (ServerVersion.is1_20_5orNewer() && im.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)) {
                // https://github.com/PaperMC/Paper/issues/10655
                im.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier("123", 1, AttributeModifier.Operation.ADD_NUMBER));
            }
        }
        var potion = item.get(ItemComponents.POTION_CONTENTS);
        if (potion != null) {
            for (PotionEffect potionEffect : potion.contents()) {
                if (im instanceof PotionMeta potionMeta) {
                    potionMeta.addCustomEffect(potionEffect, true);
                } else if (im instanceof Arrow arrow) {
                    arrow.addCustomEffect(potionEffect, true);
                } else if (im instanceof SuspiciousStewMeta m) {
                    m.addCustomEffect(potionEffect, true);
                }
            }
        }
        var basePotion = item.get(ItemComponents.BASE_POTION);
        if (basePotion != null && im instanceof PotionMeta pm){
            basePotion.apply(pm);
        }

        var color0 = item.get(ItemComponents.COLOR);
        if (color0 != null) {
            var color = color0.toBukkit();
            if (im instanceof TropicalFishBucketMeta buket) {
                ObjectUtil.applyIfNotNull(DyeColor.getByColor(color), buket::setBodyColor);
            } else if (im instanceof PotionMeta pm) {
                pm.setColor(color);
            } else if (im instanceof MapMeta map) {
                map.setColor(color);
            } else if (im instanceof LeatherArmorMeta m) {
                m.setColor(color);
            } else if (im instanceof FireworkEffectMeta effectMeta) {
                effectMeta.setEffect(FireworkEffect.builder().withColor(color).build());
            }
        }
        var enchantments = item.get(ItemComponents.ENCHANTMENTS);
        if (enchantments != null) {
            for (var entry : enchantments.enchantments()) {
                im.addEnchant(entry.enchantment(), entry.lvl(), true);
            }
        }

        var modelData = item.get(ItemComponents.MODEL_DATA);
        if (modelData != null) {
            if (ServerVersion.is1_21_4orNewer()) {
                var kringe = im.getCustomModelDataComponent();
                kringe.setFloats(modelData.floats());
                kringe.setFlags(modelData.flags());
                kringe.setStrings(modelData.strings());
                kringe.setColors(modelData.colors());
                im.setCustomModelDataComponent(kringe);
            } else {
                im.setCustomModelData(modelData.floats().get(0).intValue());
            }
        }
        if (ServerVersion.is1_20_5orNewer()) {
            //JIT DCE?
            Integer maxStackSize = item.get(ItemComponents.MAX_STACK_SIZE);
            if (maxStackSize != null) {
                im.setMaxStackSize(maxStackSize);
            }
            if (item.getBool(ItemComponents.HIDE_TOOLTIP)) {
                im.setHideTooltip(true);
            }
        }
        if (ServerVersion.is1_19_4orNewer()) {
            //JIT DCE?
            var armorTrim = item.get(ItemComponents.TRIM);
            if (armorTrim != null && armorTrim.has()) {
                if (im instanceof ArmorMeta armorMeta) {
                    armorMeta.setTrim(armorTrim.armorTrim().get());
                }
            }
        }
        if (item.getBool(ItemComponents.UNBREAKABLE)) {
            im.setUnbreakable(true);
        }

        var name = item.get(ItemComponents.NAME);
        if (name != null) {
            im.displayName(toComponent(name, placeholders));
        }
        var lore = item.get(ItemComponents.LORE);
        if (lore != null) {
            List<Component> loreComponents = new ArrayList<>();
            lore.forEachLore(line -> applyComponent(line, placeholders, loreComponents::add));
            im.lore(loreComponents);
        }
        if (im instanceof Damageable damageable) {
            damageable.setDamage(item.get(ItemComponents.DAMAGE, IntHolder.ZERO).getOrDefault(placeholders, 0));
        }
        if (im instanceof BlockStateMeta bsm) {
            BlockState state = bsm.getBlockState();
            if (state instanceof Container container) {
                ContainerComponent c = item.get(ItemComponents.CONTAINER);
                if (c != null) {
                    for (Int2ObjectMap.Entry<ItemModel> entry : c.items().int2ObjectEntrySet()) {
                        container.getInventory().setItem(entry.getIntKey(), build(entry.getValue(), placeholders));
                    }
                }
                bsm.setBlockState(container);
            }
        }

        itemStack.setItemMeta(im);
        itemStack.setAmount(item.get(ItemComponents.AMOUNT, IntHolder.ONE).getOrDefault(placeholders, 1));
        return itemStack;
    }
}
*/
