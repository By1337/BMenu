package org.by1337.bmenu.item.render;

import dev.by1337.core.ServerVersion;
import dev.by1337.plc.Placeholderable;
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
import org.bukkit.potion.PotionEffect;
import org.by1337.bmenu.item.ItemModel;
import org.by1337.bmenu.item.component.EnchantmentData;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBukkitItemRenderer implements ItemRenderer<Inventory> {
    private static final ItemStack AIR = new ItemStack(Material.AIR);
    private static final Logger log = LoggerFactory.getLogger("BMenu");

    @Override
    public void render(Inventory ctx, int slot, ItemModel item, Menu menu, Placeholderable placeholders) {
        log.info("{} update slot {}", menu.getConfig().getId(), slot);
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
        var flags = item.flags();
        if (flags != null) {
            for (ItemFlag flag : flags) {
                im.addItemFlags(flag);
            }
            if (flags.contains(ItemFlag.HIDE_ATTRIBUTES) && ServerVersion.is1_20_5orNewer()) {
                // https://github.com/PaperMC/Paper/issues/10655
                im.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier("123", 1, AttributeModifier.Operation.ADD_NUMBER));
            }
        }
        var potion = item.potionEffects();
        if (potion != null) {
            for (PotionEffect potionEffect : potion) {
                if (im instanceof PotionMeta potionMeta) {
                    potionMeta.addCustomEffect(potionEffect, true);
                } else if (im instanceof Arrow arrow) {
                    arrow.addCustomEffect(potionEffect, true);
                } else if (im instanceof SuspiciousStewMeta m) {
                    m.addCustomEffect(potionEffect, true);
                }
            }
        }
        var color = item.color();
        if (color != null) {
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
        var enchantments = item.enchantments();
        if (enchantments != null) {
            for (EnchantmentData pair : enchantments) {
                im.addEnchant(pair.enchantment(), pair.lvl(), true);
            }
        }
        var modelData = item.customModelData();
        if (modelData != null) {
            im.setCustomModelData(modelData.floats().get(0).intValue());//todo use 1.21.5 api if available
        }
        if (item.unbreakable()) {
            im.setUnbreakable(true);
        }
        itemStack.setItemMeta(im);
        var result = applyDisplay(itemStack, item, menu, placeholders);
        ctx.setItem(slot, result);
    }

    protected abstract ItemStack applyDisplay(ItemStack itemStack, ItemModel item, Menu menu, Placeholderable placeholders);
}
