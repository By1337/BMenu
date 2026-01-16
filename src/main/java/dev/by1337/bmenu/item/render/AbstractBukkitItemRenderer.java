package dev.by1337.bmenu.item.render;

import dev.by1337.bmenu.item.item.ItemModel;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.util.ObjectUtil;
import dev.by1337.core.BCore;
import dev.by1337.core.ServerVersion;
import dev.by1337.core.bridge.inventory.InventoryUtil;
import dev.by1337.plc.PlaceholderApplier;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Arrow;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String material = item.material().get(placeholders).toUpperCase();
        ItemStack itemStack = new ItemStack(Material.DIRT);
        try {
            itemStack = new ItemStack(Material.valueOf(material));//todo skulls
        } catch (Exception e) {
            log.error("Failed to create item {}", material);
        }

        ItemMeta im = itemStack.getItemMeta();
        if (im == null) {
            ctx.setItem(slot, AIR);
            return;
        }
        if (item.hasItemFlags()) {
            item.forEachItemFlags(im::addItemFlags);
            if (ServerVersion.is1_20_5orNewer() && im.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)) {
                // https://github.com/PaperMC/Paper/issues/10655
                im.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier("123", 1, AttributeModifier.Operation.ADD_NUMBER));
            }
        }
        if (item.hasPotionEffects()) {
            item.forEachPotionEffects(potionEffect -> {
                if (im instanceof PotionMeta potionMeta) {
                    potionMeta.addCustomEffect(potionEffect, true);
                } else if (im instanceof Arrow arrow) {
                    arrow.addCustomEffect(potionEffect, true);
                } else if (im instanceof SuspiciousStewMeta m) {
                    m.addCustomEffect(potionEffect, true);
                }
            });
        }

        var color0 = item.color();
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
        item.forEachEnchantments(pair -> im.addEnchant(pair.enchantment(), pair.lvl(), true));

        var modelData = item.customModelData();
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
            Integer maxStackSize = item.getMaxStackSize();
            if (maxStackSize != null) {
                im.setMaxStackSize(maxStackSize);
            }
            if (item.hideTooltip()) {
                im.setHideTooltip(true);
            }
        }
        if (ServerVersion.is1_19_4orNewer()) {
            //JIT DCE?
            var armorTrim = item.getTrim();
            if (armorTrim != null && armorTrim.has()) {
                if (im instanceof ArmorMeta armorMeta) {
                    armorMeta.setTrim(armorTrim.armorTrim().get());
                }
            }
        }
        if (item.unbreakable()) {
            im.setUnbreakable(true);
        }
        itemStack.setItemMeta(im);
        var result = applyDisplay(itemStack, item, menu, placeholders);
        ctx.setItem(slot, result);
    }

    protected abstract ItemStack applyDisplay(ItemStack itemStack, ItemModel item, Menu menu, PlaceholderApplier placeholders);

    @Override
    public void flush(Inventory ctx, Menu menu) {
        INV_UTIL.flushInv(menu.getViewer());
    }
}
